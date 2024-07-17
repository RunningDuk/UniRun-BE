package com.runningduk.unirun.api.response;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class RunningTypeGetRes {
    private final String typeName;

    public RunningTypeGetRes(RunningSchedule runningSchedule) {
        StringBuilder sb = new StringBuilder();

        sb.append("[" + runningSchedule.getType() + "] ");
        sb.append(runningSchedule.getRunningCrew() + " - ");
        sb.append(runningSchedule.getTitle());

        typeName = sb.toString();
    }
}
