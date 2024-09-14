package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.request.RunningSchedulePostReq;
import com.runningduk.unirun.api.response.CommonApiResponse;
import com.runningduk.unirun.api.response.MyRunningSchedulesGetRes;
import com.runningduk.unirun.api.service.AttendanceService;
import com.runningduk.unirun.api.service.RunningScheduleService;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.exceptions.CreatorCannotCancelException;
import com.runningduk.unirun.exceptions.DuplicateAttendingException;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;
import com.runningduk.unirun.exceptions.NotParticipatingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final RunningScheduleService runningScheduleService;
    private final AttendanceService attendanceService;

    HttpStatus httpStatus;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/running-schedules/monthly")
    public ResponseEntity<CommonApiResponse> handleGetRunningSchedulesMonthly(@RequestParam(name="year") int year, @RequestParam(name="month") int month) {
        try {
            List<Date> runningDateList = runningScheduleService.getRunningScheduleMonthly(year, month);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(runningDateList)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to fetch monthly running schedules.", e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @GetMapping("/running-schedules/daily")
    public ResponseEntity<CommonApiResponse> handleGetRunningSchedulesDaily(
            @RequestParam(name="year") int year,
            @RequestParam(name="month") int month,
            @RequestParam(name="day") int day) {
        try {
            List<RunningSchedule> runningScheduleList = runningScheduleService.getRunningScheduleByDate(year, month, day);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("SUCCESS")
                    .data(runningScheduleList)
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to fetch daily running schedules.", e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @GetMapping("/my-running-schedules")
    public ResponseEntity<CommonApiResponse> getMyRunningSchedules(HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");

            List<MyRunningSchedulesGetRes> runningScheduleList = new ArrayList<>();

            List<RunningSchedule> userCreatedSchedules = runningScheduleService.getRunningScheduleListByUserId(userId);

            for (RunningSchedule runningSchedule : userCreatedSchedules) {
                int daysUtilRunningDate = runningScheduleService.checkDaysLeft(runningSchedule.getRunningDate());
                MyRunningSchedulesGetRes res = MyRunningSchedulesGetRes.builder()
                                .runningSchedule(runningSchedule)
                                .isCreater(true)
                                .isParticipant(false)
                                .daysUntilRunningDate(daysUtilRunningDate)
                                .build();
                runningScheduleList.add(res);
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
                runningScheduleList.add(res);
            }

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(runningScheduleList)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to fetch my running schedule.", e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @DeleteMapping("/running-schedule/{runningScheduleId}")
    public ResponseEntity<CommonApiResponse> handleDeleteRunningSchedule(@PathVariable(name="runningScheduleId") int runningScheduleId, HttpSession httpSession) {
        try {
            String userId = (String) httpSession.getAttribute("userId");

            RunningSchedule runningSchedule = runningScheduleService.getRunningScheduleById(runningScheduleId);
            if (!runningSchedule.getUserId().equals(userId)) {
                httpStatus = HttpStatus.FORBIDDEN;

                return CommonApiResponse.builder()
                        .statusCode(httpStatus.value())
                        .data(null)
                        .message("You do not have permission to delete this running schedule.")
                        .build().toEntity(httpStatus);
            }

            runningScheduleService.deleteRunningSchedule(runningScheduleId);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("SUCCESS")
                    .data(null)
                    .build().toEntity(httpStatus);
        } catch (NoSuchRunningScheduleException e) {
            log.error("Failed to delete running schedule for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.NOT_FOUND;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message(e.getMessage())
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to delete running schedule for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @PostMapping("/running-schedule/{runningScheduleId}/attend")
    public ResponseEntity<CommonApiResponse> handleAttendRunningSchedule(@PathVariable(name="runningScheduleId") int runningScheduleId, HttpSession httpSession) {
        try {
            String userId = (String) httpSession.getAttribute("userId");

            attendanceService.attendRunningSchedule(runningScheduleId, userId);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (NoSuchRunningScheduleException e) {
            log.error("Failed to attend running schedule for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.NOT_FOUND;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message(e.getMessage())
                    .data(null)
                    .build().toEntity(httpStatus);
        } catch (DuplicateAttendingException e) {
            log.error("Failed to attend running schedule for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.CONFLICT;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message(e.getMessage())
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to attend running schedule for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @DeleteMapping("/running-schedule/{runningScheduleId}/unattend")
    public ResponseEntity<CommonApiResponse> handleUnattendRunningSchedule(@PathVariable(name="runningScheduleId") int runningScheduleId, HttpSession httpSession) {
        try {
            String userId = (String) httpSession.getAttribute("userId");

            attendanceService.unattendRunningSchedule(runningScheduleId, userId);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (NoSuchRunningScheduleException e) {
            log.error("Failed to unattend running schedule for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.NOT_FOUND;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message(e.getMessage())
                    .build().toEntity(httpStatus);
        } catch (CreatorCannotCancelException e) {
            log.error("Creator attempted to cancel their own schedule participation for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.FORBIDDEN;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message(e.getMessage())
                    .build().toEntity(httpStatus);
        } catch (NotParticipatingException e) {
            log.error("Attempted to cancel participation for a schedule the user is not participating in for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.CONFLICT;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message(e.getMessage())
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to unattend running schedule for running_schedule_id " + runningScheduleId, e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @PostMapping("/running-schedule")
    public ResponseEntity<CommonApiResponse> handlePostRunningSchedule(@Valid @RequestBody RunningSchedulePostReq req, BindingResult bindingResult, HttpSession httpSession) {
        try {
            String userId = (String) httpSession.getAttribute("userId");

            if (bindingResult.hasErrors()) {
                log.error("Failed to post running schedule");

                List<String> errorMessages = bindingResult.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());

                httpStatus = HttpStatus.BAD_REQUEST;
                return CommonApiResponse.builder()
                        .statusCode(httpStatus.value())
                        .data(null)
                        .message(errorMessages.toString())
                        .build().toEntity(httpStatus);
            }

            req.validateDateAndTime();

            RunningSchedule runningSchedule = req.toEntity();
            runningSchedule.setUserId(userId);
            runningScheduleService.addRunningSchedule(runningSchedule);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (IllegalArgumentException e) {
            log.error("Failed to post running schedule", e);

            httpStatus = HttpStatus.BAD_REQUEST;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message(e.getMessage())
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to post running schedule", e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }
}
