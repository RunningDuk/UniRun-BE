package com.runningduk.unirun.domain.repository;

import com.runningduk.unirun.domain.entity.Attendance;
import com.runningduk.unirun.domain.entity.Gps;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    public List<Attendance> findByUserId(String userId);
}
