package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.response.CommonApiResponse;
import com.runningduk.unirun.api.response.RunningDataGetRes;
import com.runningduk.unirun.api.service.GPSService;
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

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/my-running")
public class MyRunningController {
    HttpStatus httpStatus = HttpStatus.OK;

    private final RunningDataService runningDataService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/runnings")
    public ResponseEntity<CommonApiResponse> getMyRunningDataList(HttpSession httpSession) {
        String userId = null;
        try {
            userId = (String) httpSession.getAttribute("userId");
            List<RunningData> myRunningDataList = runningDataService.getRunningDataListByUserId(userId);

            log.info("Successfully fetched my running data list for user {}", userId);

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .data(myRunningDataList)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to fetch my running data list for user {}", userId, e);

            return CommonApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .message("나의 러닝 기록 조회에 실패하였습니다.")
                    .build().toEntity(httpStatus);
        }
    }

    @GetMapping("/running/{runningDataId}")
    public ResponseEntity<CommonApiResponse> getRunningData(@PathVariable(name="runningDataId") int runningDataId, HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");

            RunningData runningData = runningDataService.getRunningDataById(runningDataId);
            log.info("runningData: {}", runningData);

            if (runningData.getUserId() == null || runningData.getUserId().isBlank() || runningData.getUserId().isEmpty()) {
                log.info("running data user id is null");
                double cal = runningDataService.calculateCaloriesBurned(runningData.getTotalTime(),
                        runningData.getTotalKm(),
                        userId);

                runningData.setCal(cal);
                runningData.setUserId(userId);

                runningDataService.saveRunningData(runningData);
            }

            Date runningDate = runningData.getRunningDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd E");
            String formattedDate = formatter.format(runningDate);

            RunningDataGetRes data = RunningDataGetRes.builder()
                            .runningDate(formattedDate)
                                    .totalTime(runningData.getTotalTime())
                                            .totalKm(Math.round(runningData.getTotalKm() * 100.0) / 100.0)
                                                    .cal(Math.round(runningData.getCal() * 100.0) / 100.0)
                                                            .build();

            log.info("Success to get running data for id {} : {}", runningData.getRunningDataId(), runningData);

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .data(data)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (NoSuchRunningDataException e) {
            log.error("Failed to get running data", e);
            return CommonApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .data(null)
                    .message(e.getMessage())
                    .build().toEntity(httpStatus);
        } catch (Exception e) {
            log.error("Failed to get running data", e);

            return CommonApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @DeleteMapping("/{runningDataId}")
    public ResponseEntity<CommonApiResponse> deleteRunningData(@PathVariable int runningDataId, HttpSession httpSession) {
        try {
            String userId = (String) httpSession.getAttribute("userId");

            RunningData runningData = runningDataService.getRunningDataById(runningDataId);

            if (!runningData.getUserId().equals(userId)) {
                return CommonApiResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .data(null)
                        .message("You do not have permission to delete this running data.")
                        .build().toEntity(httpStatus);
            }

            runningDataService.deleteRunningDataById(runningDataId);

            log.info("Successfully deleted running data list for runningDataId {}", runningDataId);

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .data(null)
                    .message("SUCCESS")
                    .build().toEntity(httpStatus);
        } catch (NoSuchRunningDataException e) {
            log.error("Failed to delete running data for runningDataId {}", runningDataId);

            return CommonApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .data(null)
                    .message(e.getMessage())
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
