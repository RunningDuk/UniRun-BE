package com.runningduk.unirun.domain.repository;

import com.runningduk.unirun.domain.entity.Gps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GpsRepository extends JpaRepository<Gps, Integer> {
    public List<Gps> findByRunningDataIdOrderByTime(int runningDataId);
}
