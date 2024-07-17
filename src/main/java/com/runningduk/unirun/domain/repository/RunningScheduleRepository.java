package com.runningduk.unirun.domain.repository;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RunningScheduleRepository extends JpaRepository<RunningSchedule, Integer> {
    public List<RunningSchedule> findByUserId(String userId);
}
