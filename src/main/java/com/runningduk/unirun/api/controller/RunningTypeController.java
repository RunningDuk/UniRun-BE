package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.request.RunningNamePostReq;
import com.runningduk.unirun.api.response.CommonApiResponse;
import com.runningduk.unirun.api.response.RunningTypeGetRes;
import com.runningduk.unirun.api.service.AttendanceService;
import com.runningduk.unirun.api.service.RunningDataService;
import com.runningduk.unirun.api.service.RunningScheduleService;
import com.runningduk.unirun.domain.entity.RunningData;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.exceptions.NoSuchRunningDataException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/running")
public class RunningTypeController {
    HttpStatus httpStatus = HttpStatus.OK;

    private final AttendanceService attendanceService;
    private final RunningDataService runningDataService;
    private final RunningScheduleService runningScheduleService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/types")
    public ResponseEntity<CommonApiResponse> getTypes(HttpSession httpSession) {
        String userId = (String) httpSession.getAttribute("userId");

        List<RunningSchedule> rsList = new ArrayList<>();
        List<RunningSchedule> attendedSchedule = attendanceService.getRunningScheduleListByUserId(userId);
        List<RunningSchedule> createdSchedules = runningScheduleService.getRunningScheduleListByUserId(userId);

        rsList.addAll(attendedSchedule);
        rsList.addAll(createdSchedules);

        LocalDate today = LocalDate.now();

        // 오늘 날짜의 러닝 스케줄만 필터링
        rsList = rsList.stream()
                .filter(runningSchedule -> runningSchedule.getRunningDate().toLocalDate().equals(today))
                .collect(Collectors.toList());

        List<RunningTypeGetRes> typeNames = new ArrayList<>();
        typeNames.add(new RunningTypeGetRes("직접 입력"));
        for (RunningSchedule runningSchedule : rsList) {
            typeNames.add(new RunningTypeGetRes(runningSchedule));

            log.debug("사용자 RunningSchedule : " + runningSchedule);
        }

        try {
            log.info("Successfully fetched running types for user {}", userId);

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .message("SUCCESS")
                    .data(typeNames)
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to fetch running types for user {}", userId, e);

            return CommonApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("러닝 타입 조회에 실패하였습니다.")
                    .data(null)
                    .build().toEntity(httpStatus);
        }
    }

    @PatchMapping("/{runningDataId}/name")
    public ResponseEntity<CommonApiResponse> runningNameAdd(@RequestBody RunningNamePostReq requestDto, @PathVariable int runningDataId) {
        try {
            String newName = requestDto.getRunningName();

            RunningData runningData = runningDataService.getRunningDataById(runningDataId);
            runningData.setRunningName(newName);

            runningDataService.saveRunningData(runningData);

            log.info("Successfully updated running name for runningDataId {}", runningDataId);

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .data(null)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (NoSuchRunningDataException e) {
            log.error("Failed to update running name for runningDataId {}", runningDataId, e);

            return CommonApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .data(null)
                    .message("러닝 이름 저장에 실패하였습니다.")
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to add running name", e);

            return CommonApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }
}
