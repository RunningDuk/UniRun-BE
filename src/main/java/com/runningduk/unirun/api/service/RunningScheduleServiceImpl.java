package com.runningduk.unirun.api.service;

import com.runningduk.unirun.api.controller.RunningTypeController;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.domain.repository.RunningScheduleRepository;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RunningScheduleServiceImpl implements RunningScheduleService {
    private final RunningScheduleRepository runningScheduleRepository;

    public List<RunningSchedule> getRunningScheduleListByUserId(String userId) {
        return runningScheduleRepository.findByUserId(userId);
    }

    public List<RunningSchedule> getRunningScheduleList() {
        return runningScheduleRepository.findAll();
    }

    public RunningSchedule getRunningScheduleById(int runningScheduleId) throws NoSuchRunningScheduleException {
        Optional<RunningSchedule> result = runningScheduleRepository.findById(runningScheduleId);

        if (result.isPresent()) {
            return result.get();
        } else {
            throw new NoSuchRunningScheduleException(runningScheduleId);
        }
    }

    public int checkDaysLeft(Date runningDate) {
        LocalDate eventDate = runningDate.toLocalDate();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());    // 오늘 날짜 가져오기

        // 오늘부터 이벤트 날짜까지 남은 일수 계산
        return (int) ChronoUnit.DAYS.between(today, eventDate);
    }

    public boolean isUserCreater(RunningSchedule runningSchedule, String userId) {
        return (runningSchedule.getUserId().equals(userId));
    }
}
