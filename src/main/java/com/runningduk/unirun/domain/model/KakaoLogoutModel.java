package com.runningduk.unirun.domain.model;

import lombok.Data;
@Data
public class KakaoLogoutModel {
    private String clientId;
    private String logoutUri;
    private String logoutRedirectUri;
}
