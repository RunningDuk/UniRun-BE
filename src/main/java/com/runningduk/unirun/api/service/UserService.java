package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.model.UserModel;

public interface UserService {
    public UserModel getKakaoId(String code);
    public int insertUser(UserModel userModel);
    public int updateUser(UserModel userModel);
    public int deleteUser(String code);
}
