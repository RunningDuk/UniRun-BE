package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.Attendance;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.exceptions.DuplicateAttendingException;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;

import java.util.List;

public interface AttendanceService {
    public List<RunningSchedule> getRunningScheduleListByUserId(String userId);

    boolean isUserParticipant(int runningScheduleId, String userId);

    void attendRunningSchedule(int runningScheduleId, String userId) throws NoSuchRunningScheduleException, DuplicateAttendingException;
}
