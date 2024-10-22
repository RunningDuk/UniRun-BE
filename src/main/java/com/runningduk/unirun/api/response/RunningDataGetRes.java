package com.runningduk.unirun.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Time;

@Getter
@AllArgsConstructor
@Builder
public class RunningDataGetRes {
    private String runningDate;
    private Time totalTime;
    private double totalKm;
    private double cal;
}
