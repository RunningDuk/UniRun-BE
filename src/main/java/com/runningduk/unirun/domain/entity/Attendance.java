package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    @Id
    @Column(name="attendance_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int attendanceId;

    @Column(name="running_schedule_id", insertable = false, updatable = false)
    private int runningScheduleId;

    @Column(name="user_id")
    private String userId;

    @ManyToOne
    @JoinColumn(name="running_schedule_id", insertable = true, updatable = true)
    private RunningSchedule runningSchedule;
}
