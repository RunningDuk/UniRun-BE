package com.runningduk.unirun.api.service;

import com.runningduk.unirun.common.DateUtils;
import com.runningduk.unirun.domain.entity.Attendance;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.domain.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;

    public List<RunningSchedule> getRunningScheduleListByUserId(String userId) {
        List<Attendance> attendanceList = attendanceRepository.findByUserId(userId);
        List<RunningSchedule> runningScheduleList = new ArrayList<>();
        for (Attendance attendance : attendanceList) {
            runningScheduleList.add(attendance.getRunningSchedule());
        }

        LocalDate today = LocalDate.now();
        return runningScheduleList.stream()
                .filter(schedule -> !DateUtils.convertToLocalDate(schedule.getRunningDate()).isBefore(today))
                .collect(Collectors.toList());
    }

    public boolean isUserParticipant(int runningScheduleId, String userId) {
        return attendanceRepository.existsByRunningScheduleIdAndUserId(runningScheduleId, userId);
    }
}
