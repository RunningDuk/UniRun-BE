package com.runningduk.unirun.api.response;

import com.runningduk.unirun.domain.entity.RunningData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyRunningGetRes {
    private List<RunningData> myRunningDataList;
}
