package com.runningduk.unirun.api.controller;

import com.runningduk.unirun.api.request.UserPatchReq;
import com.runningduk.unirun.api.response.CommonApiResponse;
import com.runningduk.unirun.api.response.SaveResultModel;
import com.runningduk.unirun.domain.model.KakaoLogoutModel;
import com.runningduk.unirun.domain.model.UserModel;
import com.runningduk.unirun.api.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
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
        try {
            String code = (String) requestData.get("code");
            log.info("Kakao authorization code received: {}", code);  // 인가 코드 로그

            UserModel userInfo = userService.getKakaoId(code, request);
            log.info("Fetched user info: {}", userInfo);

            // userId를 세션에 저장
            session.setAttribute("userId", userInfo.getUserId());
            log.info("User ID {} saved in session", userInfo.getUserId());  // 세션에 저장된 userId 로그

            if (!userInfo.isUnirunUser()) {     // 회원가입이 필요한 경우
                log.warn("Registration required for user ID: {}", userInfo.getUserId());  // 회원가입 필요 로그
                return CommonApiResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message("Registration required.")
                        .data(null)
                        .build()
                        .toEntity(httpStatus);
            }

            log.info("Login successful for user ID: {}", userInfo.getUserId());  // 로그인 성공 로그
            log.info("/user/auth 의 Session id: {}", session.getId());

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .message("SUCCESS")
                    .data(userInfo)
                    .build()
                    .toEntity(httpStatus);
        } catch (HttpClientErrorException e) {      // 인가코드가 만료된 경우
            log.error("Kakao authorization code expired or not found", e);  // 인가코드 오류 로그
            return CommonApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Kakao authorization code not found or expired.")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        } catch (Exception e) {     // 서버 내부 오류
            log.error("Internal server error occurred", e);  // 서버 오류 로그
            return CommonApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .message("An internal server error occurred. Please try again later.")
                    .build().toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user/auth", method = RequestMethod.GET)
    public String getCode(@RequestParam(name = "code") String code) {
        log.info("Kakao authorization code: {}", code);  // 인가 코드 로그
        return code;
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ResponseEntity<CommonApiResponse> selectUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        log.info("User ID {} retrieved from session", userId);  // 세션에서 userId 로그

        UserModel userModel = userService.selectUser(userId);

        if (userModel != null) {
            log.info("User found for user ID: {}", userId);  // 사용자 찾음 로그
            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .message("success")
                    .data(userModel)
                    .build()
                    .toEntity(httpStatus);
        } else {
            log.warn("User not found for user ID: {}", userId);  // 사용자 없음 로그
            return CommonApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("사용자 없음")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.PATCH)
    public ResponseEntity<CommonApiResponse> saveUser(@RequestBody UserPatchReq req, HttpSession session) {
        SaveResultModel saveResultModel = new SaveResultModel();
        String userId = (String) session.getAttribute("userId");
        log.info("User ID {} retrieved from session for registration", userId);  // 회원가입 세션 로그

        UserModel userModel = req.toModel();
        userModel.setUserId(userId);

        log.info("UserModel: {}", userModel);  // 회원가입 모델 로그

        int result = userService.updateUser(userModel);

        if (result == 1) {
            log.info("User registration successful for user ID: {}", userId);  // 회원가입 성공 로그
            return CommonApiResponse.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("SUCCESS")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        } else {
            log.error("User registration failed for user ID: {}", userId);  // 회원가입 실패 로그
            return CommonApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("회원가입 실패")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user/logout", method = RequestMethod.DELETE)
    public ResponseEntity<CommonApiResponse> logout(HttpSession session, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("Cookie Name: " + cookie.getName());
                log.info("Cookie Value: " + cookie.getValue());
            }
        }

        log.info("/user/logout 의 Session id: {}", session.getId());    // 세션 ID
        log.info("Logging out user ID: {}", session.getAttribute("userId"));  // 로그아웃 로그
        try {
            String refreshToken = (String) session.getAttribute("refreshToken");

            log.info("refreshToken is : " + refreshToken);

            Map<String, String> tokens = userService.refreshTokens(refreshToken);
            String accessToken = tokens.get("accessToken");
            refreshToken = tokens.get("refreshToken");

            int logoutResult = userService.kakaoLogout(accessToken);
            log.info("Kakao logout successful");

            session.invalidate();
            log.info("Session invalidated");

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .message("SUCCESS")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);

        } catch (Exception e) {
            log.error("Logout failed", e);  // 로그아웃 실패 로그
            return CommonApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("로그아웃 실패")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user/update", method = RequestMethod.PATCH)
    public ResponseEntity<CommonApiResponse> updateUser(@RequestBody UserPatchReq req, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        log.info("Updating user with userId: {}", userId);

        UserModel userModel = req.toModel();
        userModel.setUserId(userId);
        log.debug("User model to update: {}", userModel);

        int result = userService.updateUser(userModel);

        if (result == 1) {
            log.info("User update successful for userId: {}", userId);
            return CommonApiResponse.builder()
                    .status(HttpStatus.CREATED.value())
                    .message("SUCCESS")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        } else {
            log.error("User update failed for userId: {}", userId);
            return  CommonApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("회원가입 실패")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        }
    }

    @RequestMapping(value = "/user/delete", method = RequestMethod.DELETE)
    public ResponseEntity<CommonApiResponse> deleteUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        log.info("Deleting user with ID: {}", userId);  // 사용자 삭제 로그

        log.info("Attempting to delete user with userId: {}", userId);
        int result = userService.deleteUser(userId);

        if (result == 1) {
            log.info("User deletion successful for userId: {}", userId);

            session.invalidate();
            log.info("Session invalidated");

            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .message("회원 탈퇴 성공")
                    .data(result)
                    .build()
                    .toEntity(httpStatus);
        } else {
            log.error("User deletion failed for user ID: {}", userId);  // 사용자 삭제 실패 로그

            httpStatus = HttpStatus.BAD_REQUEST;
            log.error("User deletion failed for userId: {}", userId);
            return CommonApiResponse.builder()
                    .status(httpStatus.value())
                    .message("회원 탈퇴 실패")
                    .data(null)
                    .build()
                    .toEntity(httpStatus);
        }
    }
}