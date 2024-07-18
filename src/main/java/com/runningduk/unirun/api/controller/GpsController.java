package com.runningduk.unirun.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningduk.unirun.common.Day;
import com.runningduk.unirun.common.RunningStatus;
import com.runningduk.unirun.api.message.LocationMessage;
import com.runningduk.unirun.api.message.SummeryMessage;
import com.runningduk.unirun.api.message.StatusMessage;
import com.runningduk.unirun.api.message.DistanceMessage;
import com.runningduk.unirun.api.service.GPSService;
import com.runningduk.unirun.api.service.RunningDataService;
import com.runningduk.unirun.domain.entity.Gps;
import com.runningduk.unirun.domain.entity.RunningData;
import com.runningduk.unirun.api.service.GpsScheduler;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class GpsController extends TextWebSocketHandler {
//    logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

//    스케줄링 작업을 관리
    private final GpsScheduler gpsScheduler;

//    Service 클래스
    private final GPSService gpsService;
    private final RunningDataService runningDataService;

//    JSON 변환을 위한 ObjectMapper 객체
    private final ObjectMapper objectMapper = new ObjectMapper();

//    WebSocket 세션을 관리용 맵
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private RunningStatus status;

//    클라이언트와 WebSocket 연결이 열릴 때 호출
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        session.getAttributes().put("locationData", new HashMap<String, Double>()); // locationData 맵 초기화
        logger.info("WebSocket connection established: {}", session.getId());
    }

//    클라이언트로부터 메시지를 받을 때 호출
    @Override
    protected void handleTextMessage(WebSocketSession socketSession, TextMessage message) throws Exception {
        super.handleTextMessage(socketSession, message);
        logger.info("Received message from session {}: {}", socketSession.getId(), message.getPayload());

        String payload = message.getPayload();  // 메시지 페이로드를 JSON으로 파싱
        Map<String, Object> receivedData = objectMapper.readValue(payload, Map.class);

        String messageType = (String) receivedData.get("type");
        Map<String, Object> payloadData = (Map<String, Object>) receivedData.get("payload");

        HttpSession httpSession = (HttpSession) socketSession.getAttributes().get("http_session");
        String userId = (String) httpSession.getAttribute("userId");

        socketSession.getAttributes().put("userId", userId);
        logger.info("Processing message of type: {}", messageType);

        switch (messageType) {
            case "status":
                StatusMessage statusReq = objectMapper.convertValue(payloadData, StatusMessage.class);
                processStatusData(socketSession, statusReq);
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

//    러닝 상태에 따른 처리
// 러닝 상태에 따른 처리
    private void processStatusData(WebSocketSession socketSession, StatusMessage statusReq) throws Exception {
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
            socketSession.sendMessage(new TextMessage("REQUEST_GPS_DATA"));
        }
        logger.info("Processed status data: {}", statusReq);
    }

//    러닝 시작 상태 프로세스
    private void processStart(WebSocketSession socketSession) {
        logger.info("Starting running session for session: {}", socketSession.getId());
        Date date = new Date(System.currentTimeMillis());

        String userId = (String) socketSession.getAttributes().get("userId");   // HttpSession에서 userId 가져오기

        RunningData runningData = RunningData.builder()
                .runningDataId(0)
                .userId(userId)
                .cal(0)
                .totalTime(new Time(System.currentTimeMillis()))
                .totalKm(0)
                .runningDate(date)
                .runningName("running name")
                .build();

        int runningDataId = runningDataService.saveRunningData(runningData);
        socketSession.getAttributes().put("runningDataId", runningDataId);   // 세션에 RunningDataID 저장

        gpsScheduler.addSession(socketSession.getId(), socketSession);  // GpsScheduler에 세션 추가
        gpsScheduler.startScheduler();
        logger.info("Running session started for session: {}", socketSession.getId());
    }

//    Scheduler 시작
    private void processRestart() {
        logger.info("Restarting running session.");
        gpsScheduler.startScheduler();
    }

//    Scheduler 정지
    private void processPause(WebSocketSession socketSession) throws IOException {
        logger.info("Pausing running session for session: {}", socketSession.getId());
        gpsScheduler.stopScheduler();
        socketSession.sendMessage(new TextMessage("REQUEST_GPS_DATA"));
    }

//      러닝 종료 상태 프로세스
    private void processFinish(WebSocketSession socketSession) throws IOException, IllegalAccessException {
        logger.info("Finishing running session for session: {}", socketSession.getId());

//        WebSocket Session에서 runningDataId와 userId 값 가져오기
        int runningDataId = (int) socketSession.getAttributes().get("runningDataId");
        String userId = (String) socketSession.getAttributes().get("userId");

//        WebSocket Session에서 distance 값 가져오기
        Map<String, Double> locationData = (Map<String, Double>) socketSession.getAttributes().get("locationData");
        double distance = locationData.get("distance");

//        총 러닝 시간과 소모 칼로리 계산
        Time totalTime = runningDataService.calculateTotalTime(runningDataId);
        double cal = runningDataService.calculateCaloriesBurned(totalTime, distance, userId);

        RunningData runningData = RunningData.builder()
                .runningDataId(runningDataId)
                .userId(userId)
                .cal(cal)
                .totalTime(totalTime)
                .totalKm(distance)
                .runningName(new Date(System.currentTimeMillis()).toString())
                .runningDate(new Date(System.currentTimeMillis()))
                .build();

//        runningDataService 변경
        runningDataService.saveRunningData(runningData);

//        FE에 Date, Time, Distance, Cal 정보 넘기기
        responseWithRunningSummary(socketSession, runningData);

//        세션 종료
        socketSession.close();

        logger.info("Running session finished and closed for session: {}", socketSession.getId());
    }

//    Gps 데이터 저장 및 거리 갱신
    private Map<String, Double> processLocationData(WebSocketSession socketSession, LocationMessage runningLocationReq) {
        int runningDataId = (int) socketSession.getAttributes().get("runningDataId");
        String userId = (String) socketSession.getAttributes().get("userId");

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Gps gps = Gps.builder()
                .gpsId(0)
                .userId(userId)
                .time(timestamp)
                .latitude(runningLocationReq.getLatitude())
                .longitude(runningLocationReq.getLongitude())
                .runningDataId(runningDataId)
                .build();
        gpsService.saveGps(gps);    // GPS 데이터 저장

//        session 업데이트
        Map<String, Double> locationData = (Map<String, Double>) socketSession.getAttributes().get("locationData");
        locationData = gpsService.updateLocationData(locationData, runningLocationReq.getLatitude(), runningLocationReq.getLongitude());
        socketSession.getAttributes().put("locationData", locationData);

        logger.info("Processed location data for user {}, runningDataId {}, latitude {}, longitude {}", userId, runningDataId, runningLocationReq.getLatitude(), runningLocationReq.getLongitude());

        return locationData;
    }

//    갱신된 거리 Response로 넘기기
    private void responseWithDistance(WebSocketSession socketSession) throws IOException {
        Map<String, Double> locationData = (Map<String, Double>) socketSession.getAttributes().get("locationData");
        double distance = locationData.get("distance");
        DistanceMessage distanceRes = DistanceMessage.builder()
                .totalDistance(distance)
                .build();
        String responsePayload = objectMapper.writeValueAsString(distanceRes);
        socketSession.sendMessage(new TextMessage(responsePayload));

        logger.info("Responded with distance {} for session {}", distance, socketSession.getId());
    }

//    러닝 종료 화면 데이터 Response로 넘기기
    private void responseWithRunningSummary(WebSocketSession socketSession, RunningData runningData) throws IllegalAccessException, IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String date = dateFormat.format(runningData.getRunningDate());

        String dayOfWeek = Day.fromDate(runningData.getRunningDate()).toString();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(runningData.getTotalTime());

        int runningDataId = (int) socketSession.getAttributes().get("runningDataId");

        SummeryMessage summeryMessage = SummeryMessage.builder()
                .date(date)
                .time(time)
                .dayOfWeek(dayOfWeek)
                .distance(runningData.getTotalKm())
                .cal(runningData.getCal())
                .runningDataId(runningDataId)
                .build();

        String responsePayload = objectMapper.writeValueAsString(summeryMessage);
        socketSession.sendMessage(new TextMessage(responsePayload));

        logger.info("Responded with running summary for session {}", socketSession.getId());
    }
}
