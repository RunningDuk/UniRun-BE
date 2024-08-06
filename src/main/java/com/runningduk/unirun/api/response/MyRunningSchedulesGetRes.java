package com.runningduk.unirun.api.response;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyRunningSchedulesGetRes {
    private final RunningSchedule runningSchedule;
    private final boolean isCreater;
    private final boolean isParticipant;
}
