package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.exceptions.NoSuchRunningScheduleException;

import java.sql.Date;
import java.util.List;

public interface RunningScheduleService {
    public List<RunningSchedule> getRunningScheduleListByUserId(String userId);

    public List<RunningSchedule> getRunningScheduleList();

    RunningSchedule getRunningScheduleById(int runningScheduleId) throws NoSuchRunningScheduleException;

    int checkDaysLeft(Date runningDate);

    boolean isUserCreater(RunningSchedule runningSchedule, String userId);

    List<Date> getRunningScheduleMonthly(int year, int month);
}
