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

    List<RunningSchedule> getRunningScheduleByDate(int year, int month, int day);

    void deleteRunningSchedule(int runningScheduleId) throws NoSuchRunningScheduleException;

    void addRunningSchedule(RunningSchedule runningSchedule);
}
