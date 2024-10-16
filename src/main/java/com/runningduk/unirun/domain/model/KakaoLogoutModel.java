package com.runningduk.unirun.domain.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KakaoLogoutModel {
    private String clientId;
    private String logoutUri;
    private String logoutRedirectUri;
}
