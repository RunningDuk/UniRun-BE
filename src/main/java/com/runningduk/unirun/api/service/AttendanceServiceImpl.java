package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.Attendance;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.domain.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

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

        return runningScheduleList;
    }

    public boolean isUserParticipant(int runningScheduleId, String userId) {
        return attendanceRepository.existsByRunningScheduleIdAndUserId(runningScheduleId, userId);
    }
}
