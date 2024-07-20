package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.service.RunningDataService;
import com.runningduk.unirun.domain.entity.RunningData;
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
@RequestMapping("/my-running")
public class MyRunningController {
    HashMap<String, Object> result;

    private final RunningDataService runningDataService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyRunningDataList(HttpSession httpSession) {
        String userId = null;
        result = new HashMap<>();
        try {
            userId = (String) httpSession.getAttribute("userId");
            List<RunningData> myRunningDataList = runningDataService.getRunningDataListByUserId(userId);

            result.put("myRunningDataList", myRunningDataList);

            log.info("Successfully fetched my running data list for user {}", userId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch my running data list for user {}", userId, e);

            result.put("error", "나의 러닝 기록 조회에 실패하였습니다.");

            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/{runningDataId}")
    public ResponseEntity<Map<String, Object>> deleteRunningData(@PathVariable int runningDataId) {
        try {
            result = new HashMap<>();

            runningDataService.deleteRunningDataById(runningDataId);

            result.put("msg", "러닝 기록 삭제에 성공하였습니다.");

            log.info("Successfully deleted running data list for runningDataId {}", runningDataId);

            return ResponseEntity.ok(result);
        } catch (NoSuchRunningDataException e) {
            log.error("Failed to delete running data for runningDataId {}", runningDataId);

            result.put("error", "runningDataId " + runningDataId + "에 해당하는 러닝 기록이 없습니다.");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
