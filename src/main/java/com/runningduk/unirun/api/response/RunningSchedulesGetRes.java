package com.runningduk.unirun.api.response;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;

@Getter
@AllArgsConstructor
public class RunningSchedulesGetRes {
    private final int runningScheduleId;
    private final String type;
    private final Date runningDate;

    public RunningSchedulesGetRes(RunningSchedule runningSchedule) {
        this.runningScheduleId = runningSchedule.getRunningScheduleId();
        this.type = runningSchedule.getType();
        this.runningDate = runningSchedule.getRunningDate();
    }
}
