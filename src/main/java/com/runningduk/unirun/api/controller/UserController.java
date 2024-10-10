package com.runningduk.unirun.api.controller;
import static org.springframework.http.HttpStatus.OK;

import com.runningduk.unirun.api.response.CommonApiResponse;
import com.runningduk.unirun.api.response.SaveResultModel;
import com.runningduk.unirun.common.CommonResVO;
import com.runningduk.unirun.domain.entity.User;
import com.runningduk.unirun.domain.model.KakaoLogoutModel;
import com.runningduk.unirun.domain.model.UserModel;
import com.runningduk.unirun.api.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    HttpStatus httpStatus = HttpStatus.OK;

    @RequestMapping(value = "/user/auth", method = RequestMethod.POST)
    public ResponseEntity<CommonApiResponse> getKakaoProfile(@RequestBody Map<String, Object> requestData, HttpServletRequest request, HttpSession session) {
        String code = (String) requestData.get("code");
        System.out.println("code ====>>>>"+code);

        UserModel userInfo = userService.getKakaoId(code,request);
        if (userInfo == null) {
            return CommonApiResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("카카오 로그인 실패")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        }
        else {
            session.setAttribute("userId", userInfo.getUserId());

            return  CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("커카오 로그인 성공")
                    .data(userInfo)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ResponseEntity<CommonApiResponse> selectUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        UserModel userModel = userService.selectUser(userId);

        if (userModel != null) {
            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("success")
                    .data(userModel)
                    .build()
                    .toEntity(httpStatus);
        }
        else {
            return  CommonApiResponse.builder()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .message("사용자 없음")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public ResponseEntity<CommonApiResponse> saveUser(@RequestBody UserModel userModel) {
        SaveResultModel saveResultModel = new SaveResultModel();
        userModel.setNickname(userModel.getNickname());

        int result = userService.insertUser(userModel);

        if (result == 1) {
            return CommonApiResponse.builder()
                    .statusCode(HttpStatus.CREATED.value())
                    .message("회원가입 성공")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        }
        else {
            return  CommonApiResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("회원가입 실패")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value="/user/logout", method = RequestMethod.DELETE)
    public ResponseEntity<CommonApiResponse> logout(HttpServletRequest request) {
        //서비스 실행 - 세션에서 access token 가져와서 카카오 접속해서 토컨 만료, http session도 만료
        HttpSession session = request.getSession();
        try {
            session.invalidate();
            KakaoLogoutModel kakaoLogoutModel = new KakaoLogoutModel();
            kakaoLogoutModel = userService.getKakaLogOutInfo();

            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("success")
                    .data(kakaoLogoutModel)
                    .build()
                    .toEntity(httpStatus);

        }catch (Exception e) {
            return CommonApiResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("로그아웃 실패")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        }

    }

    @RequestMapping(value = "/user/update", method = RequestMethod.PUT)
    public ResponseEntity<CommonApiResponse> updateUser(@RequestBody UserModel userModel) {
        SaveResultModel saveResultModel = new SaveResultModel();

        int result = userService.updateUser(userModel);
        if (result == 1) {
            httpStatus = HttpStatus.OK;
            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("회원 수정 성공")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        }
        else {
            httpStatus = HttpStatus.BAD_REQUEST;
            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("회원 수정 실패")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user/delete", method = RequestMethod.DELETE)
    public ResponseEntity<CommonApiResponse> deleteUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        log.info("userId ={}",userId);
        int result = userService.deleteUser(userId);

        if (result == 1) {
            httpStatus = HttpStatus.OK;
            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("회원 탈퇴 성공")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        }
        else {
            httpStatus = HttpStatus.BAD_REQUEST;
            return CommonApiResponse.builder()
                    .statusCode(httpStatus.value())
                    .message("회원 탈퇴 실패")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        }

    }
}

