package com.runningduk.unirun.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningduk.unirun.api.message.CommonMessage;
import com.runningduk.unirun.exceptions.GPSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class GpsScheduler {
//    클라이언트 세션 관리용 맵
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

//    스케줄링 작업 관리
    private final TaskScheduler taskScheduler;

//    스케줄링된 작업을 관리 (작업 중지 등)
    private ScheduledFuture<?> scheduledTask;

    @Autowired
    public GpsScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

//    새로운 세션을 sessions 맵에 추가
    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

//    세션을 sessions 맵에서 제거
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

//    fetchGpsTask 메서드를 15초마다 실행
    public void startScheduler() {
        if (scheduledTask == null || scheduledTask.isCancelled()) {
            scheduledTask = taskScheduler.scheduleAtFixedRate(this::fetchGpsTask, 15000); // 30초마다 실행
            if (scheduledTask != null) {
                System.out.println("Scheduler started successfully");
            } else {
                System.out.println("Scheduler failed to start");
            }

        } else {
            System.out.println("Scheduler already running");
        }
    }

//    스케줄링된 작업이 있으면 이를 중지
    public void stopScheduler() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
            scheduledTask = null;
            System.out.println("Scheduler stopped");
        } else if (scheduledTask == null) {
            System.out.println("Scheduler was not running");
            System.out.println("scheduledTask is null");
        } else if (scheduledTask.isCancelled()) {
            System.out.println("Scheduler was not running");
            System.out.println("scheduledTask is cancelled");
        }
    }

//    fetchGps 메서드 호출
    private void fetchGpsTask() {
        try {
            fetchGps();
        } catch (GPSException e) {
            e.printStackTrace();
        }
    }

//    @Scheduled(fixedRate = 15000)   // 15초마다 GPS 데이터 요청
    public void fetchGps() throws GPSException {
        ObjectMapper objectMapper = new ObjectMapper();

        for (WebSocketSession session : sessions.values()) {
            try {
                CommonMessage commonMessage = CommonMessage.builder()
                                .type("REQUEST_GPS")
                                .payload(null)
                                .build();

                String jsonMessage = objectMapper.writeValueAsString(commonMessage);

                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                e.printStackTrace();
                throw new GPSException("fetch GPS error");
            }
        }
    }
}
