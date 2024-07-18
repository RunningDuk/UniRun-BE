package com.runningduk.unirun.domain.model;

import lombok.Data;

@Data
public class UserModel {
    private String userId;
    private String nickname;
    private String userUniName;
    private String gender;
    private String birthYear;
    private double height;
    private double weight;
    private String goal;
}
