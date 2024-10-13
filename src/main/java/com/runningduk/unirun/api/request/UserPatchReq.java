package com.runningduk.unirun.api.request;

import com.runningduk.unirun.domain.model.UserModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPatchReq {
    private String nickname;
    private String userUniName;
    private String gender;
    private String birthYear;
    private double height;
    private double weight;
    private String goal;
    private String walletAddress;

     public UserModel toModel() {
        return UserModel.builder()
                .nickname(nickname)
                .userUniName(userUniName)
                .gender(gender)
                .birthYear(birthYear)
                .height(height)
                .weight(weight)
                .goal(goal)
                .walletAddress(walletAddress)
                .isUnirunUser(true)
                .build();
    }
}