package com.runningduk.unirun.api.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummeryMessage {
    private String date;
    private String dayOfWeek;
    private String time;
    private double distance;
    private double cal;
    private int runningDataId;
}
