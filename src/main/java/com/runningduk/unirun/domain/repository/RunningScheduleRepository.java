package com.runningduk.unirun.domain.repository;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface RunningScheduleRepository extends JpaRepository<RunningSchedule, Integer>, JpaSpecificationExecutor<RunningSchedule> {
    public List<RunningSchedule> findByUserId(String userId);

    @Query("SELECT DISTINCT rs.runningDate FROM RunningSchedule rs WHERE rs.runningDate BETWEEN :startDate AND :endDate ORDER BY rs.runningDate")
    public List<Date> findDistinctRunningDatesByMonth(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    public List<RunningSchedule> findRunningScheduleByRunningDate(Date date);
}
