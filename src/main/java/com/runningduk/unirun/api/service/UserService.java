package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.model.KakaoLogoutModel;
import com.runningduk.unirun.domain.model.UserModel;
import com.runningduk.unirun.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public interface UserService {
    public UserModel getKakaoId(String code, HttpServletRequest request) throws UserNotFoundException;
    public UserModel selectUser(String userId);
    public int insertUser(UserModel userModel);
    public int updateUser(UserModel userModel);
    public int deleteUser(String code);
    public int kakaoLogout(String accessToken);
    public KakaoLogoutModel getKakaLogOutInfo();
    Map<String, String> refreshTokens(String refreshToken) throws Exception;
}
