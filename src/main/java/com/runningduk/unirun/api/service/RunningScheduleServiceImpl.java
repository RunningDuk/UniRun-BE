package com.runningduk.unirun.api.service;

import com.runningduk.unirun.common.DateUtils;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.domain.repository.RunningScheduleRepository;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RunningScheduleServiceImpl implements RunningScheduleService {
    private final RunningScheduleRepository runningScheduleRepository;

    public List<RunningSchedule> getRunningScheduleListByUserId(String userId) {
        List<RunningSchedule> runningScheduleList = runningScheduleRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();
        return runningScheduleList.stream()
                .filter(schedule -> !DateUtils.convertToLocalDate(schedule.getRunningDate()).isBefore(today))
                .collect(Collectors.toList());
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

    public List<Date> getRunningScheduleMonthly(int year, int month) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = year + "-" + String.format("%02d", month) + "-" + "01";

        LocalDate date = LocalDate.parse(dateStr, formatter);
        LocalDate firstDateOfMonth = date.withDayOfMonth(1);
        LocalDate lastDateOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        Date firstDateParam = java.sql.Date.valueOf(firstDateOfMonth);
        Date lastDateParam = java.sql.Date.valueOf(lastDateOfMonth);

        return runningScheduleRepository.findDistinctRunningDatesByMonth(firstDateParam, lastDateParam);
    }

    public List<RunningSchedule> getRunningScheduleByDate(int year, int month, int day) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);

        LocalDate date = LocalDate.parse(dateStr, formatter);
        Date dateParam = java.sql.Date.valueOf(date);

        return runningScheduleRepository.findRunningScheduleByRunningDate(dateParam);
    }
}
