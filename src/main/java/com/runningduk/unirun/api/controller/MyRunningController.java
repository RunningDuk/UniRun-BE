package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.response.CommonApiResponse;
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
    HttpStatus httpStatus;

    private final RunningDataService runningDataService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping
    public ResponseEntity<CommonApiResponse> getMyRunningDataList(HttpSession httpSession) {
        String userId = null;
        try {
            userId = (String) httpSession.getAttribute("userId");
            List<RunningData> myRunningDataList = runningDataService.getRunningDataListByUserId(userId);

            log.info("Successfully fetched my running data list for user {}", userId);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(myRunningDataList)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to fetch my running data list for user {}", userId, e);

            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("나의 러닝 기록 조회에 실패하였습니다.")
                    .build().toEntity(httpStatus);
        }
    }

    @DeleteMapping("/{runningDataId}")
    public ResponseEntity<CommonApiResponse> deleteRunningData(@PathVariable int runningDataId) {
        try {
            runningDataService.deleteRunningDataById(runningDataId);

            log.info("Successfully deleted running data list for runningDataId {}", runningDataId);

            httpStatus = HttpStatus.OK;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (NoSuchRunningDataException e) {
            log.error("Failed to delete running data for runningDataId {}", runningDataId);

            httpStatus = HttpStatus.NOT_FOUND;

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .data(null)
                    .message("runningDataId " + runningDataId + "에 해당하는 러닝 기록이 없습니다.")
                    .build().toEntity(httpStatus);
        }
    }
}
