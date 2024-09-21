package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name="university")
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="university_id")
    private int universityId;

    @Column(name="university_name")
    private String universityName;

    @Column(name="image_url")
    private String imageUrl;
}
