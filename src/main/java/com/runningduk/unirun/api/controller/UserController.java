package com.runningduk.unirun.api.controller;
import static org.springframework.http.HttpStatus.OK;
import com.runningduk.unirun.api.response.SaveResultModel;
import com.runningduk.unirun.common.CommonResVO;
import com.runningduk.unirun.domain.entity.User;
import com.runningduk.unirun.domain.model.KakaoLogoutModel;
import com.runningduk.unirun.domain.model.UserModel;
import com.runningduk.unirun.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final View error;

    @RequestMapping(value = "/user/auth", method = RequestMethod.POST)
    public ResponseEntity<CommonResVO<UserModel>> getKakaoProfile(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        String code = (String) requestData.get("code");
        String successOrNot = null;
        String statusCode = null;
        String errorMessage = null;

        System.out.println("code ====>>>>"+code);
        UserModel userInfo = userService.getKakaoId(code,request);
        if (userInfo == null) {
            successOrNot = "N";
            statusCode = "fail";
            errorMessage = "카카오 로그인 실패";
        }
        else {
            successOrNot = "S";
            statusCode = "success";
            errorMessage = "";
        }

        return new ResponseEntity<>(
            CommonResVO.<UserModel>builder()
                       .successOrNot(successOrNot)
                       .statusCode(statusCode)
                       .errorMessage(errorMessage)
                       .data(userInfo)
                       .build()
                  ,OK);
        //return userInfo;
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public ResponseEntity<CommonResVO<UserModel>> selectUser(@PathVariable("userId") String userId) {
        UserModel userModel = userService.selectUser(userId);
        String successOrNot = null;
        String statusCode = null;
        String errorMessage = null;

        if (userModel != null) {
            successOrNot = "Y";
            statusCode = "success";
            errorMessage = "";
        }
        else {
            successOrNot = "N";
            statusCode = "fail";
            errorMessage = "사용자 없음";
        }
        return new ResponseEntity<>(
                CommonResVO.<UserModel>builder()
                        .successOrNot(successOrNot)
                        .statusCode(statusCode)
                        .errorMessage(errorMessage)
                        .data(userModel)
                        .build()
                ,OK);
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public ResponseEntity<CommonResVO<Integer>> saveUser(@RequestBody UserModel userModel) {
        SaveResultModel saveResultModel = new SaveResultModel();
        userModel.setNickname(userModel.getNickname());

        String successOrNot = null;
        String statusCode = null;
        String errorMessage = null;

        int result = userService.insertUser(userModel);

        if (result == 1) {
            successOrNot = "Y";
            statusCode = "success";
            errorMessage = "";
        }
        else {
            successOrNot = "N";
            statusCode = "fail";
            errorMessage = "회원가입 실패";
        }
        return new ResponseEntity<>(
         CommonResVO.<Integer>builder()
                .successOrNot(successOrNot)
                .statusCode(statusCode)
                .errorMessage(errorMessage)
                .data(result)
                .build()
                ,OK);
    }

    @RequestMapping(value="/user/logout", method = RequestMethod.DELETE)
    public ResponseEntity<CommonResVO<KakaoLogoutModel>> logout(HttpServletRequest request) {
        String successOrNot = null;
        String statusCode = null;
        String errorMessage = null;
        //서비스 실행 - 세션에서 access token 가져와서 카카오 접속해서 토컨 만료, http session도 만료
        KakaoLogoutModel kakaoLogoutModel = new KakaoLogoutModel();
        kakaoLogoutModel = userService.getKakaLogOutInfo();
        HttpSession session = request.getSession();
        session.invalidate();
        if (session != null) {
            successOrNot = "N";
            statusCode = "fail";
            errorMessage = "로그아웃 실패";
        }
        else {
            successOrNot = "Y";
            statusCode = "success";
            errorMessage = "";
        }

        return new ResponseEntity<>(
                CommonResVO.<KakaoLogoutModel>builder()
                        .successOrNot(successOrNot)
                        .statusCode(statusCode)
                        .errorMessage(errorMessage)
                        .data(kakaoLogoutModel)
                        .build(),
                OK
        );
    }

    @RequestMapping(value = "/user/update", method = RequestMethod.PUT)
    public ResponseEntity<CommonResVO<Integer>> updateUser(@RequestBody UserModel userModel) {
        SaveResultModel saveResultModel = new SaveResultModel();
        String successOrNot = null;
        String statusCode = null;
        String errorMessage = null;
        int result = userService.updateUser(userModel);
        if (result == 1) {
            successOrNot = "Y";
            statusCode = "success";
            errorMessage = "";
        }
        else {
            successOrNot = "N";
            statusCode = "fail";
            errorMessage = "회원 수정 실패";
        }
        return new ResponseEntity<>(
                CommonResVO.<Integer>builder()
                        .successOrNot(successOrNot)
                        .statusCode(statusCode)
                        .errorMessage(errorMessage)
                        .data(result)
                        .build()
                ,OK);
    }

    @RequestMapping(value = "/user/delete/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<CommonResVO<Integer>> deleteUser(@PathVariable("userId") String userId) {
        log.info("userId ={}",userId);
        String successOrNot = null;
        String statusCode = null;
        String errorMessage = null;
        int result = userService.deleteUser(userId);

        if (result == 1) {
            successOrNot = "Y";
            statusCode = "success";
            errorMessage = "";
        }
        else {
            successOrNot = "N";
            statusCode = "fail";
            errorMessage = "회원 탈퇴 실패";
        }
        return new ResponseEntity<>(
                CommonResVO.<Integer>builder()
                        .successOrNot(successOrNot)
                        .statusCode(statusCode)
                        .errorMessage(errorMessage)
                        .data(result)
                        .build()
                ,OK);
    }
}

