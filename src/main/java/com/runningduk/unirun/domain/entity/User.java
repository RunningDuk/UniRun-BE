package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name="user_id")
    private String userId;

    private String goal;

    @Column(name="nickname")
    private String nickName;

    private double height;

    private double weight;

    @Column(name="birth_year")
    private int birthYear;

    private char gender;

    @Column(name="user_uni_name")
    private String uniName;

    @Column(name="wallet_address")
    private String walletAddress;
}