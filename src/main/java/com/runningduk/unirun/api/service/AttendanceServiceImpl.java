package com.runningduk.unirun.api.service;

import com.runningduk.unirun.common.DateUtils;
import com.runningduk.unirun.domain.entity.Attendance;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.domain.repository.AttendanceRepository;
import com.runningduk.unirun.exceptions.CreatorCannotCancelException;
import com.runningduk.unirun.exceptions.DuplicateAttendingException;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;
import com.runningduk.unirun.exceptions.NotParticipatingException;
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
    private final RunningScheduleService runningScheduleService;

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

    public void attendRunningSchedule(int runningScheduleId, String userId) throws NoSuchRunningScheduleException, DuplicateAttendingException {
        RunningSchedule runningSchedule = runningScheduleService.getRunningScheduleById(runningScheduleId);

        boolean isAttending = attendanceRepository.existsByRunningScheduleIdAndUserId(runningScheduleId, userId);
        if (runningSchedule.getUserId().equals(userId) || isAttending) {
            throw new DuplicateAttendingException(String.valueOf(runningScheduleId));
        }

        Attendance newAttendance = Attendance.builder()
                .runningScheduleId(runningScheduleId)
                .userId(userId)
                .runningSchedule(runningSchedule)
                .build();

        System.out.println("attendance service - new attendance : " + newAttendance);

        attendanceRepository.save(newAttendance);
    }

    public void unattendRunningSchedule(int runningScheduleId, String userId)
            throws NoSuchRunningScheduleException, CreatorCannotCancelException, NotParticipatingException {
        RunningSchedule runningSchedule = runningScheduleService.getRunningScheduleById(runningScheduleId);

        if (runningSchedule.getUserId().equals(userId)) {
            throw new CreatorCannotCancelException();
        } else if (!attendanceRepository.existsByRunningScheduleIdAndUserId(runningScheduleId, userId)) {
            throw new NotParticipatingException(String.valueOf(runningScheduleId));
        }

        attendanceRepository.deleteByRunningScheduleIdAndUserId(runningScheduleId, userId);
    }
}
