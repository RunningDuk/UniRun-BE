package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="running_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunningSchedule {
    @Id
    @Column(name="running_schedule_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int runningScheduleId;

    @Column(name="user_id")
    private String userId;

    @Column(name="title")
    private String title;

    @Column(name="running_date")
    private Date runningDate;

    @Column(name="start_time")
    private Time startTime;

    @Column(name="end_time")
    private Time endTime;

    @Column(name="type")
    private String type;

    @Column(name="audience_type")
    private String audienceType;

    private String place;

    @Column(name="running_crew")
    private String runningCrew;
}
