package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "gps")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gps {
    @Id
    @Column(name="gps_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gpsId;

    @Column(name="user_id")
    private String userId;

    private Timestamp time;

    private double latitude;

    private double longitude;

    @Column(name="running_data_id")
    private int runningDataId;
}
