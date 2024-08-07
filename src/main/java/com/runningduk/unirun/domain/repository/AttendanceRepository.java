package com.runningduk.unirun.domain.repository;

import com.runningduk.unirun.domain.entity.Attendance;
import com.runningduk.unirun.domain.entity.Gps;
import com.runningduk.unirun.domain.entity.RunningSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    public List<Attendance> findByUserId(String userId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a WHERE a.runningScheduleId = :runningScheduleId AND a.userId = :userId")
    boolean existsByRunningScheduleIdAndUserId(@Param("runningScheduleId") int runningScheduleId, @Param("userId") String userId);
}
