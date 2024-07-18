package com.runningduk.unirun.api.controller;
import com.runningduk.unirun.api.response.SaveResultModel;
import com.runningduk.unirun.domain.model.UserModel;
import com.runningduk.unirun.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user/auth", method = RequestMethod.POST)
    public UserModel getKakaoProfile(@RequestBody Map<String, Object> requestData) {
        String code = (String) requestData.get("code");
        System.out.println("code ====>>>>"+code);
        UserModel userInfo = userService.getKakaoId(code);
        return userInfo;
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public SaveResultModel saveUser(@RequestBody UserModel userModel) {
        SaveResultModel saveResultModel = new SaveResultModel();
        userModel.setNickname(userModel.getNickname());

        int result = userService.insertUser(userModel);
        if (result == 1) {
           saveResultModel.setResult("Y");
        }
        else {
            saveResultModel.setResult("N");
        }
        return saveResultModel;
    }

    @RequestMapping(value = "/user/update", method = RequestMethod.PUT)
    public SaveResultModel updateUser(@RequestBody UserModel userModel) {
        SaveResultModel saveResultModel = new SaveResultModel();
        int result = userService.updateUser(userModel);
        if (result == 1) {
            saveResultModel.setResult("Y");
        } else {
            saveResultModel.setResult("N");
        }
        return saveResultModel;
    }

    @RequestMapping(value = "/user/delete", method = RequestMethod.DELETE)
    public SaveResultModel deleteUser(@RequestBody Map<String, Object> requestData) {
        String userId = (String) requestData.get("userId");
        SaveResultModel saveResultModel = new SaveResultModel();
        int result = userService.deleteUser(userId);
        if (result == 1) {
            saveResultModel.setResult("Y");
        } else {
            saveResultModel.setResult("N");
        }
        return saveResultModel;
    }
}

