package com.runningduk.unirun.domain.repository;

import com.runningduk.unirun.domain.entity.RunningData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RunningDataRepository extends JpaRepository<RunningData, Integer> {
    public List<RunningData> findByUserId(String userId);
}
