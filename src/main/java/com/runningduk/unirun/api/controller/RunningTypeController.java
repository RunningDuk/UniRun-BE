package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.request.RunningNamePostReq;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/running")
public class RunningTypeController {
    HashMap<String, Object> result;

    private final AttendanceService attendanceService;
    private final RunningDataService runningDataService;
    private final RunningScheduleService runningScheduleService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getTypes(HttpSession httpSession) {
        String userId = (String) httpSession.getAttribute("userId");

        List<RunningSchedule> rsList = new ArrayList<>();
        List<RunningSchedule> attendedSchedule = attendanceService.getRunningScheduleListByUserId(userId);
        List<RunningSchedule> createdSchedules = runningScheduleService.getRunningScheduleListByUserId(userId);

        rsList.addAll(attendedSchedule);
        rsList.addAll(createdSchedules);

        List<RunningTypeGetRes> responseList = new ArrayList<>();
        responseList.add(new RunningTypeGetRes("직접 입력"));
        for (RunningSchedule runningSchedule : rsList) {
            responseList.add(new RunningTypeGetRes(runningSchedule));

            log.debug("사용자 RunningSchedule : " + runningSchedule);
        }

        result = new HashMap<>();

        try {
            result.put("typeNames", responseList);

            log.info("Successfully fetched running types for user {}", userId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch running types for user {}", userId, e);

            result.put("error", "러닝 타입 조회에 실패하였습니다.");

            return ResponseEntity.badRequest().body(result);
        }
    }

    @PatchMapping("/{runningDataId}/name")
    public ResponseEntity<Map<String, Object>> runningNameAdd(@RequestBody RunningNamePostReq requestDto, @PathVariable int runningDataId) {
        result = new HashMap<>();

        try {
            String newName = requestDto.getRunningName();

            RunningData runningData = runningDataService.getRunningDataById(runningDataId);
            runningData.setRunningName(newName);

            runningDataService.saveRunningData(runningData);

            result.put("msg", "러닝 이름 저장에 성공하였습니다.");

            log.info("Successfully updated running name for runningDataId {}", runningDataId);

            return ResponseEntity.ok(result);
        } catch (NoSuchRunningDataException e) {
            log.error("Failed to update running name for runningDataId {}", runningDataId, e);

            result.put("error", "러닝 이름 저장에 실패하였습니다.");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
