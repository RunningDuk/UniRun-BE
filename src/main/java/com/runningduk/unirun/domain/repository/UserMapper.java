package com.runningduk.unirun.domain.repository;

import com.runningduk.unirun.domain.model.UserModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    UserModel selectUser(String userId);
    int insertUser(UserModel userModel);
    int updateUser(UserModel userModel);
    int deleteUser(String userId);
}
