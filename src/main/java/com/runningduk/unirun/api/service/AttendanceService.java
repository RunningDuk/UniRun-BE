package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.RunningSchedule;

import java.util.List;

public interface AttendanceService {
    public List<RunningSchedule> getRunningScheduleListByUserId(String userId);
}
