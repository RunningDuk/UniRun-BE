package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.RunningSchedule;

import java.util.List;

public interface RunningScheduleService {
    public List<RunningSchedule> getRunningScheduleListByUserId(String userId);

    public List<RunningSchedule> getRunningScheduleList();
}
