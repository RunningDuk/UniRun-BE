package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.response.MyRunningSchedulesGetRes;
import com.runningduk.unirun.api.service.AttendanceService;
import com.runningduk.unirun.api.service.RunningScheduleService;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.exceptions.CreatorCannotCancelException;
import com.runningduk.unirun.exceptions.DuplicateAttendingException;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;
import com.runningduk.unirun.exceptions.NotParticipatingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class RunningScheduleController {
    private final RunningScheduleService runningScheduleService;
    private final AttendanceService attendanceService;

    HashMap<String, Object> result;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/running-schedules/monthly")
    public ResponseEntity<Map<String, Object>> handleGetRunningSchedulesMonthly(@RequestParam(name="year") int year, @RequestParam(name="month") int month) {
        try {
            result = new HashMap<>();

            List<Date> runningDateList = runningScheduleService.getRunningScheduleMonthly(year, month);

            result.put("runningDates", runningDateList);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch monthly running schedules.", e);

            result.put("error", "An internal server error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/running-schedules/daily")
    public ResponseEntity<Map<String, Object>> handleGetRunningSchedulesDaily(
            @RequestParam(name="year") int year,
            @RequestParam(name="month") int month,
            @RequestParam(name="day") int day) {
        try {
            result = new HashMap<>();

            List<RunningSchedule> runningScheduleList = runningScheduleService.getRunningScheduleByDate(year, month, day);

            result.put("runningSchedules", runningScheduleList);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch daily running schedules.", e);

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
                int daysUtilRunningDate = runningScheduleService.checkDaysLeft(runningSchedule.getRunningDate());
                MyRunningSchedulesGetRes res = MyRunningSchedulesGetRes.builder()
                                .runningSchedule(runningSchedule)
                                .isCreater(true)
                                .isParticipant(false)
                                .daysUntilRunningDate(daysUtilRunningDate)
                                .build();
                resList.add(res);
            }

            List<RunningSchedule> attendedSchedule = attendanceService.getRunningScheduleListByUserId(userId);

            for (RunningSchedule runningSchedule : attendedSchedule) {
                int daysUtilRunningDate = runningScheduleService.checkDaysLeft(runningSchedule.getRunningDate());
                MyRunningSchedulesGetRes res = MyRunningSchedulesGetRes.builder()
                        .runningSchedule(runningSchedule)
                        .isCreater(false)
                        .isParticipant(true)
                        .daysUntilRunningDate(daysUtilRunningDate)
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

    @DeleteMapping("/running-schedule/{runningScheduleId}")
    public ResponseEntity<Map<String, Object>> handleDeleteRunningSchedule(@PathVariable(name="runningScheduleId") int runningScheduleId, HttpSession httpSession) {
        try {
            result = new HashMap<>();

            String userId = (String) httpSession.getAttribute("userId");
            if (userId == null) {
                result.put("error", "Login is required.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            RunningSchedule runningSchedule = runningScheduleService.getRunningScheduleById(runningScheduleId);
            if (!runningSchedule.getUserId().equals(userId)) {
                result.put("error", "You do not have permission to delete this running schedule.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            runningScheduleService.deleteRunningSchedule(runningScheduleId);

            result.put("message", "러닝 일정 삭제에 성공했습니다.");

            return ResponseEntity.ok(result);
        } catch (NoSuchRunningScheduleException e) {
            log.error("Failed to delete running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } catch (Exception e) {
            log.error("Failed to delete running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", "An internal server error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/running-schedule/{runningScheduleId}/attend")
    public ResponseEntity<Map<String, Object>> handleAttendRunningSchedule(@PathVariable(name="runningScheduleId") int runningScheduleId, HttpSession httpSession) {
        try {
            result = new HashMap<>();

            String userId = (String) httpSession.getAttribute("userId");
            if (userId == null) {
                result.put("error", "Login is required.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            attendanceService.attendRunningSchedule(runningScheduleId, userId);

            result.put("message", "러닝 스케줄 참석에 성공하였습니다.");

            return ResponseEntity.ok(result);
        } catch (NoSuchRunningScheduleException e) {
            log.error("Failed to attend running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } catch (DuplicateAttendingException e) {
            log.error("Failed to attend running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        } catch (Exception e) {
            log.error("Failed to attend running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", "An internal server error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @DeleteMapping("/running-schedule/{runningScheduleId}/unattend")
    public ResponseEntity<Map<String, Object>> handleUnattendRunningSchedule(@PathVariable(name="runningScheduleId") int runningScheduleId, HttpSession httpSession) {
        try {
            result = new HashMap<>();

            String userId = (String) httpSession.getAttribute("userId");
            if (userId == null) {
                result.put("error", "Login is required.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            attendanceService.unattendRunningSchedule(runningScheduleId, userId);

            result.put("message", "러닝 취소에 성공하였습니다.");

            return ResponseEntity.ok(result);
        } catch (NoSuchRunningScheduleException e) {
            log.error("Failed to unattend running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } catch (CreatorCannotCancelException e) {
            log.error("Creator attempted to cancel their own schedule participation for running_schedule_id " + runningScheduleId, e);

            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        } catch (NotParticipatingException e) {
            log.error("Attempted to cancel participation for a schedule the user is not participating in for running_schedule_id " + runningScheduleId, e);

            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception e) {
            log.error("Failed to unattend running schedule for running_schedule_id " + runningScheduleId, e);

            result.put("error", "An internal server error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
