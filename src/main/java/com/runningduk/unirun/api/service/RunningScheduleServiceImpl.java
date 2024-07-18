package com.runningduk.unirun.api.service;

import com.runningduk.unirun.api.controller.RunningTypeController;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import com.runningduk.unirun.domain.repository.RunningScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RunningScheduleServiceImpl implements RunningScheduleService {
    private final RunningScheduleRepository runningScheduleRepository;

    public List<RunningSchedule> getRunningScheduleListByUserId(String userId) {
        return runningScheduleRepository.findByUserId(userId);
    }
}
