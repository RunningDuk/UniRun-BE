package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name="reward")
public class Reward {
    @Id
    @Column(name="reward_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rewardId;

    @Column(name="receiver_id")
    private String receiverId;

    private int amount;

    @Column(name="transaction_hash")
    private String transactionHash;

    @Column(name="rewarded_at")
    private Date rewardedAt;
}
