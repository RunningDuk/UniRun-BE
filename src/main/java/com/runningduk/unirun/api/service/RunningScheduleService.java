package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;

import java.util.List;

public interface RunningScheduleService {
    public List<RunningSchedule> getRunningScheduleListByUserId(String userId);

    public List<RunningSchedule> getRunningScheduleList();

    RunningSchedule getRunningScheduleById(int runningScheduleId) throws NoSuchRunningScheduleException;
}
