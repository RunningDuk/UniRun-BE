package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="running_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class RunningData {
    @Id
    @Column(name="running_data_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int runningDataId;

    @Column(name="user_id")
    private String userId;

    private double cal;

    @Column(name="total_time")
    private Time totalTime;

    @Column(name="total_km")
    private double totalKm;

    @Column(name="running_name")
    private String runningName;

    @Column(name="running_date")
    private Date runningDate;
}
