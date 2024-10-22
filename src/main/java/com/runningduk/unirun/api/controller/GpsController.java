package com.runningduk.unirun.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningduk.unirun.api.message.*;
import com.runningduk.unirun.common.RunningStatus;
import com.runningduk.unirun.api.service.GPSService;
import com.runningduk.unirun.api.service.RunningDataService;
import com.runningduk.unirun.domain.entity.Gps;
import com.runningduk.unirun.domain.entity.RunningData;
import com.runningduk.unirun.api.service.GpsScheduler;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GpsController extends TextWebSocketHandler {
    // logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 스케줄링 작업을 관리
    private final GpsScheduler gpsScheduler;

    // Service 클래스
    private final GPSService gpsService;
    private final RunningDataService runningDataService;

    // JSON 변환을 위한 ObjectMapper 객체
    private final ObjectMapper objectMapper = new ObjectMapper();

    // WebSocket 세션을 관리용 맵
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 러닝 상태
    private RunningStatus status;

    // 총 러닝 시간
    private Time totalTime;

    // 클라이언트와 WebSocket 연결이 열릴 때 호출
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        session.getAttributes().put("locationData", new HashMap<String, Double>()); // locationData 맵 초기화
        logger.info("WebSocket connection established: sessionId = {}, locationData initialized", session.getId());
    }

    // 클라이언트로부터 메시지를 받을 때 호출
    @Override
    protected void handleTextMessage(WebSocketSession socketSession, TextMessage message) throws Exception {
        super.handleTextMessage(socketSession, message);
        logger.info("Received message from session {}: {}", socketSession.getId(), message.getPayload());

        String payload = message.getPayload();  // 메시지 페이로드를 JSON으로 파싱
        Map<String, Object> receivedData = objectMapper.readValue(payload, Map.class);

        String messageType = (String) receivedData.get("type");
        Map<String, Object> payloadData = (Map<String, Object>) receivedData.get("payload");

        logger.info("Message type: {}, Payload: {}", messageType, payloadData);

        switch (messageType) {
            case "status":
                StatusMessage statusReq = objectMapper.convertValue(payloadData, StatusMessage.class);
                processStatusData(socketSession, statusReq, receivedData);
                break;
            case "location":
                LocationMessage locationMessage = objectMapper.convertValue(payloadData, LocationMessage.class);
                processLocationData(socketSession, locationMessage);
                responseWithDistance(socketSession);

                if (status == RunningStatus.PAUSE) {    // 러닝 중지 상태 처리
                    Map<String, Double> locationData = (Map<String, Double>) socketSession.getAttributes().get("locationData");
                    locationData.put("latitude", null);
                    locationData.put("longitude", null);
                    socketSession.getAttributes().put("locationData", locationData);
                } else if (status == RunningStatus.FINISH) {    // 러닝 종료 상태 처리
                    processFinish(socketSession);
                }
                break;
            default:
                String errorMessage = "Invalid message type received.";
                logger.warn("Received unknown message type: {}", messageType);
                socketSession.sendMessage(new TextMessage(errorMessage));
                break;
        }
    }


    //    클라이언트와의 WebSocket 연결이 닫힐 때 호출
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        gpsScheduler.removeSession(session.getId());    // GpsScheduler에서 세션 제거
        logger.info("WebSocket connection closed: {}", session.getId());
    }

    // 러닝 상태에 따른 처리
    private void processStatusData(WebSocketSession socketSession, StatusMessage statusReq, Map<String, Object> receivedData) throws Exception {
        if (statusReq.isStart()) {          // 러닝 Start 버튼 클릭 시
            status = RunningStatus.START;
            processStart(socketSession);
        } else if (statusReq.isPause()) {   // 러닝 Pause 버튼 클릭 시
            status = RunningStatus.PAUSE;
            processPause(socketSession);
        } else if (statusReq.isRestart()) { // 러닝 Restart 버튼 클릭 시
            status = RunningStatus.RESTART;
            processRestart();
        } else {                            // 러닝 Finish 버튼 클릭 시
            status = RunningStatus.FINISH;
            gpsScheduler.stopScheduler();   // Scheduler 정지

            // 프론트로부터 총 러닝 시간 받아오기
            String receivedTotalTime = (String) receivedData.get("totalRunningTime");
            totalTime = Time.valueOf(receivedTotalTime);

            processFinish(socketSession);
        }
        logger.info("Processed status data: {}", statusReq);
    }

    //    러닝 시작 상태 프로세스
    private void processStart(WebSocketSession socketSession) {
        logger.info("Starting running session for session: {}", socketSession.getId());
        Date date = new Date(System.currentTimeMillis());

        RunningData runningData = RunningData.builder()
                .runningDataId(0)
                .cal(0)
                .totalTime(new Time(System.currentTimeMillis()))
                .totalKm(0)
                .runningDate(date)
                .runningName("running name")
                .build();

        int runningDataId = runningDataService.saveRunningData(runningData);
        logger.info("Running data saved: runningDataId = {}, sessionId = {}", runningDataId, socketSession.getId());

        socketSession.getAttributes().put("runningDataId", runningDataId);   // 세션에 RunningDataID 저장

        gpsScheduler.addSession(socketSession.getId(), socketSession);  // GpsScheduler에 세션 추가
        gpsScheduler.startScheduler();
    }

    //    Scheduler 시작
    private void processRestart() {
        logger.info("Restarting running session.");
        gpsScheduler.startScheduler();
    }

    // Scheduler 정지
    private void processPause(WebSocketSession socketSession) throws IOException {
        logger.info("Pausing running session for session: {}", socketSession.getId());
        gpsScheduler.stopScheduler();
        socketSession.sendMessage(new TextMessage("REQUEST_GPS_DATA"));
    }

    // 러닝 종료 상태 프로세스
    private void processFinish(WebSocketSession socketSession) throws IOException, IllegalAccessException {
        // WebSocket Session에서 runningDataId와 userId 값 가져오기
        int runningDataId = (int) socketSession.getAttributes().get("runningDataId");

        logger.info("Finished running session for runningDataId: {}", runningDataId);

        // WebSocket Session에서 distance 값 가져오기
        Map<String, Double> locationData = (Map<String, Double>) socketSession.getAttributes().get("locationData");
        double distance = 0;
        if (locationData.get("distance") != null) {
            distance = locationData.get("distance");
        }

        RunningData runningData = RunningData.builder()
                .runningDataId(runningDataId)
                .totalTime(totalTime)
                .totalKm(distance)
                .runningName(new Date(System.currentTimeMillis()).toString())
                .runningDate(new Date(System.currentTimeMillis()))
                .build();

        // runningDataService 변경
        runningDataService.saveRunningData(runningData);

        // FE에 Date, Time, Distance, Cal 정보 넘기기
        requestWSEnd(socketSession, runningData);
    }

    // Gps 데이터 저장 및 거리 갱신
    private Map<String, Double> processLocationData(WebSocketSession socketSession, LocationMessage runningLocationReq) {
        int runningDataId = (int) socketSession.getAttributes().get("runningDataId");

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Gps gps = Gps.builder()
                .gpsId(0)
                .time(timestamp)
                .latitude(runningLocationReq.getLatitude())
                .longitude(runningLocationReq.getLongitude())
                .runningDataId(runningDataId)
                .build();
        gpsService.saveGps(gps);    // GPS 데이터 저장

        // session 업데이트
        Map<String, Double> locationData = (Map<String, Double>) socketSession.getAttributes().get("locationData");
        if (locationData != null) {
            locationData = gpsService.updateLocationData(locationData, runningLocationReq.getLatitude(), runningLocationReq.getLongitude());
        }
        socketSession.getAttributes().put("locationData", locationData);

        logger.info("Location data processed: latitude = {}, longitude = {}, sessionId = {}", runningLocationReq.getLatitude(), runningLocationReq.getLongitude(), socketSession.getId());
        logger.info("Updated location data: {}", locationData);

        return locationData;
    }

    // 갱신된 거리 Response로 넘기기
    private void responseWithDistance(WebSocketSession socketSession) throws IOException {
        Map<String, Double> locationData = (Map<String, Double>) socketSession.getAttributes().get("locationData");
        double distance = locationData.get("distance");

        Map data = new HashMap();
        data.put("totalDistance", distance);

        CommonMessage commonMessage = CommonMessage.builder()
                .type("distance")
                .payload(distance)
                .build();

        String responsePayload = objectMapper.writeValueAsString(commonMessage);
        socketSession.sendMessage(new TextMessage(responsePayload));

        logger.info("Responded with distance = {} for session: {}", distance, socketSession.getId());
    }

    // 러닝 종료 화면 데이터 Response로 넘기기
    private void requestWSEnd(WebSocketSession socketSession, RunningData runningData) throws IllegalAccessException, IOException {
        logger.info("Sent running end request to sessionId: {}, runningDataId: {}", socketSession.getId(), runningData.getRunningDataId());

        int runningDataId = (int) socketSession.getAttributes().get("runningDataId");

        Map data = new HashMap();
        data.put("runningDataId", runningDataId);

        CommonMessage commonMessage = CommonMessage.builder()
                .type("END")
                .payload(data)
                .build();

        String responsePayload = objectMapper.writeValueAsString(commonMessage);
        socketSession.sendMessage(new TextMessage(responsePayload));

        logger.info("Request Web Socket End {}", socketSession.getId());
    }
}
