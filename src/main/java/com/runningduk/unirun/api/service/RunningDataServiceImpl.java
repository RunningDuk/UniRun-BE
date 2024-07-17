package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.Gps;
import com.runningduk.unirun.domain.entity.RunningData;
import com.runningduk.unirun.domain.entity.User;
import com.runningduk.unirun.domain.repository.GpsRepository;
import com.runningduk.unirun.domain.repository.RunningDataRepository;
import com.runningduk.unirun.domain.repository.UserRepository;
import com.runningduk.unirun.exceptions.NoSuchRunningDataException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RunningDataServiceImpl implements RunningDataService {

    private final RunningDataRepository runningDataRepository;

    private final GpsRepository gpsRepository;

    private final UserRepository userRepository;

    @Transactional
    public int saveRunningData(RunningData runningData) {
        RunningData newRunningData = runningDataRepository.save(runningData);
        return newRunningData.getRunningDataId();
    }

    public RunningData getRunningDataById(int runningDataId) throws NoSuchRunningDataException {
        Optional<RunningData> result = runningDataRepository.findById(runningDataId);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new NoSuchRunningDataException("No such running_data");
        }
    }

    public List<RunningData> getRunningByUserId(String userId) {
        return runningDataRepository.findByUserId(userId);
    }

    public Time calculateTotalTime(int runningDataId) {
        List<Gps> gpsList = gpsRepository.findByRunningDataIdOrderByTime(runningDataId);

        Timestamp startTime = gpsList.get(0).getTime();
        Timestamp endTime = gpsList.get(gpsList.size() - 1).getTime();

        LocalDateTime startDateTime = startTime.toLocalDateTime();
        LocalDateTime endDateTime = endTime.toLocalDateTime();

        Duration duration = Duration.between(startDateTime, endDateTime);

        LocalTime time = LocalTime.ofSecondOfDay(duration.getSeconds());

        return Time.valueOf(time);
    }

    public double calculateCaloriesBurned(Time totalTime, Double distance, String userId) {
        // LocalTime으로 변환
        LocalTime localTime = totalTime.toLocalTime();
        // 초 단위로 변환
        long totalSeconds = localTime.toSecondOfDay();
        // 시간 단위로 변환
        double totalHours = totalSeconds / 3600.0;

        double kmPerHour = distance / totalHours;

        double met = selectMET(kmPerHour);

        User user = null;
        Optional<User> result = userRepository.findById(userId);
        if (result.isPresent()) {
            user = result.get();
        }

        double weight;
        if (user == null) {
            weight = 60;
        } else {
            weight = user.getWeight();
        }

        return met * weight * totalHours;
    }

    public double selectMET(double speed) {
        if (speed < 3.2) {
            return 2.0; // 걷기 (느린 속도)
        } else if (speed < 5.6) {
            return 3.8; // 걷기 (보통 속도)
        } else if (speed < 8.0) {
            return 6.0; // 조깅
        } else if (speed < 9.7) {
            return 8.3; // 러닝 (8 km/h)
        } else if (speed < 11.2) {
            return 9.8; // 러닝 (9.7 km/h)
        } else if (speed < 12.1) {
            return 11.0; // 러닝 (11.2 km/h)
        } else {
            return 11.5; // 러닝 (빠른 속도)
        }
    }
}
