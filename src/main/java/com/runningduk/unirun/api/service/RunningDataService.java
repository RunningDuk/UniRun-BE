package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.RunningData;
import com.runningduk.unirun.exceptions.NoSuchRunningDataException;

import java.sql.Time;
import java.util.List;

public interface RunningDataService {
    public int saveRunningData(RunningData runningData);

    public Time calculateTotalTime(int runningDataId);

    public double calculateCaloriesBurned(Time totalTime, Double distance, String userId);

    public RunningData getRunningDataById(int runningDataId) throws NoSuchRunningDataException;

    public double selectMET(double speed);

    public List<RunningData> getRunningDataListByUserId(String userId);

    public void deleteRunningDataById(int runningDataId) throws NoSuchRunningDataException;
}
