package com.runningduk.unirun.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserModel {
    @Schema(description = "사용자 ID")
    private String userId;
    @Schema(description = "닉네임")
    private String nickname;
    @Schema(description = "사용자 대학교")
    private String userUniName;
    @Schema(description = "성별")
    private String gender;
    @Schema(description = "태어난 해")
    private String birthYear;
    @Schema(description = "키")
    private double height;
    @Schema(description = "몸무게")
    private double weight;
    @Schema(description = "사용자 목표")
    private String goal;
}
