package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.response.MyRunningSchedulesGetRes;
import com.runningduk.unirun.api.response.RunningSchedulesGetRes;
import com.runningduk.unirun.api.service.AttendanceService;
import com.runningduk.unirun.api.service.RunningScheduleService;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final RunningScheduleService runningScheduleService;
    private final AttendanceService attendanceService;

    HashMap<String, Object> result;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/running-schedules")
    public ResponseEntity<Map<String, Object>> getRunningSchedules() {
        try {
            result = new HashMap<>();

            List<RunningSchedule> runningScheduleList = runningScheduleService.getRunningScheduleList();
            List<RunningSchedulesGetRes> resList = new ArrayList<>();
            for (RunningSchedule runningSchedule : runningScheduleList) {
                resList.add(new RunningSchedulesGetRes(runningSchedule));
            }

            result.put("runningScheduleList", resList);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch running schedules", e);

            result.put("error", "러닝 타입 조회에 실패하였습니다.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/running-schedule/{runningScheduleId}")
    public ResponseEntity<Map<String, Object>> getRunningSchedule(@PathVariable int runningScheduleId, HttpSession session) {
        try {
            result = new HashMap<>();

            RunningSchedule runningSchedule = runningScheduleService.getRunningScheduleById(runningScheduleId);

            int daysUntilRunning = runningScheduleService.checkDaysLeft(runningSchedule.getRunningDate());

            String userId = (String) session.getAttribute("userId");

            if (userId == null) {
                result.put("error", "Login is required.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
            boolean isParticipant = (runningScheduleService.isUserCreater(runningSchedule, userId) || attendanceService.isUserParticipant(runningScheduleId, userId));

            System.out.println(isParticipant);

            result.put("runningSchedule", runningSchedule);
            result.put("daysUntilRunning", daysUntilRunning);
            result.put("participant", isParticipant);

            return ResponseEntity.ok(result);
        } catch (NoSuchRunningScheduleException e) {
            log.error("Failed to fetch running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception e) {
            log.error("Failed to fetch running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", "An internal server error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/my-running-schedules")
    public ResponseEntity<Map<String, Object>> getMyRunningSchedules(HttpSession session) {
        try {
            result = new HashMap<>();

            String userId = (String) session.getAttribute("userId");

            if (userId == null) {
                result.put("error", "Login is required.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            List<MyRunningSchedulesGetRes> resList = new ArrayList<>();

            List<RunningSchedule> userCreatedSchedules = runningScheduleService.getRunningScheduleListByUserId(userId);

            for (RunningSchedule runningSchedule : userCreatedSchedules) {
                MyRunningSchedulesGetRes res = MyRunningSchedulesGetRes.builder()
                                .runningSchedule(runningSchedule)
                                .isCreater(true)
                                .isParticipant(false)
                                .build();
                resList.add(res);
            }

            List<RunningSchedule> attendedSchedule = attendanceService.getRunningScheduleListByUserId(userId);

            for (RunningSchedule runningSchedule : attendedSchedule) {
                MyRunningSchedulesGetRes res = MyRunningSchedulesGetRes.builder()
                        .runningSchedule(runningSchedule)
                        .isCreater(false)
                        .isParticipant(true)
                        .build();
                resList.add(res);
            }

            result.put("runningScheduleList", resList);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch my running schedule.", e);

            result.put("error", "An internal server error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
