package com.runningduk.unirun.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.runningduk.unirun.domain.model.KakaoLogoutModel;
import com.runningduk.unirun.domain.repository.UserMapper;
import com.runningduk.unirun.domain.model.UserModel;
import com.runningduk.unirun.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.logout-uri}")
    private String logoutUri;

//    @Value("${kakao.logout-redirect-uri}")
//    private String logoutRedirectUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    public UserModel getKakaoId(String code, HttpServletRequest request) throws UserNotFoundException {
        HttpSession session = request.getSession();

        // 1. 토큰 받기
        Map<String, String> tokens = getKakaoTokens(code);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        log.info("Kakao access token: {}", accessToken);
        log.info("Kakao refresh token: {}", refreshToken);

        session.setAttribute("accessToken", accessToken);
        session.setAttribute("refreshToken", refreshToken);

        // 2. userId 얻기
        String[] returnValues = getKakaoUserId(accessToken);
        String userId = returnValues[0];

        log.info("Kakao user ID: {}", userId);

        UserModel userModel = userMapper.selectUser(userId);

        if (userModel == null) {
            log.info("User not found, creating a new user with ID: {}", userId);
            userModel = UserModel.builder()
                    .userId(userId)
                    .gender("F")
                    .build();

            int result = insertUser(userModel);
            log.info("User creation result: {}", result);
        }

        return userModel;
    }

    public int kakaoLogout(String accessToken) {
        log.info("Logging out with access token: {}", accessToken);

        int returnValue = 1;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoLogoutRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v1/user/logout",
                HttpMethod.POST,
                kakaoLogoutRequest,
                String.class
        );

        log.info("Logout response: {}", response);
        return returnValue;
    }


    public KakaoLogoutModel getKakaLogOutInfo() {
        KakaoLogoutModel kakaoLogOutModel = new KakaoLogoutModel();
        kakaoLogOutModel.setClientId(kakaoClientId);
        kakaoLogOutModel.setLogoutUri(logoutUri);
//        kakaoLogOutModel.setLogoutRedirectUri(logoutRedirectUri); // 리다이렉트 생략
        return kakaoLogOutModel;
    }

    private Map<String, String> getKakaoTokens(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        log.info("Requesting Kakao tokens with clientSecret: {}", kakaoClientSecret);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        log.info("Token response: {}", response);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response.getBody().toString());

        String accessToken = element.getAsJsonObject().get("access_token").getAsString();
        String refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        log.info("Access token: {}, Refresh token: {}", accessToken, refreshToken);

        return tokens;
    }

    private String[] getKakaoUserId(String accessToken) {
        String[] returnValues = new String[2];

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        log.info("User info response: {}", response.getBody());

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response.getBody().toString());
        String userId = element.getAsJsonObject().get("id").getAsString();

        JsonElement element2 = element.getAsJsonObject().get("properties");
        String nickname = element2.getAsJsonObject().get("nickname").getAsString();

        log.info("Kakao user ID: {}, Nickname: {}", userId, nickname);

        returnValues[0] = userId;
        returnValues[1] = nickname;

        return returnValues;
    }
    public UserModel selectUser(String userId) {
        log.info("Selecting user with ID: {}", userId);
        return userMapper.selectUser(userId);
    }
    public int insertUser(UserModel userModel){
        log.info("Inserting user: {}", userModel);
        return userMapper.insertUser(userModel);
    }
    public int updateUser(UserModel userModel) {
        log.info("Updating user: {}", userModel);
        return userMapper.updateUser(userModel);
    }
    public int deleteUser(String userId){
        log.info("Deleting user with ID: {}", userId);
        return userMapper.deleteUser(userId);
    }

    public Map<String, String> refreshTokens(String refreshToken) throws Exception {
        Logger log = LoggerFactory.getLogger(UserService.class); // Logger 선언

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        RestTemplate rt = new RestTemplate();

        try {
            ResponseEntity<String> response = rt.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("Response status: {}", response.getStatusCode());
            log.info("Response body: {}", response.getBody());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            String newAccessToken = jsonNode.get("access_token").asText();
            String newRefreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : refreshToken;

            log.info("New access token: {}, New refresh token: {}", newAccessToken, newRefreshToken);

            Map<String, String> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);

            return result;
        } catch (HttpClientErrorException e) {
            log.error("Error during token refresh: {}", e.getResponseBodyAsString());
            log.error("Status code: {}", e.getStatusCode());
            throw e; // 예외를 다시 던져서 호출한 쪽에서 처리할 수 있게 함
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw e; // 다른 예외 처리
        }
    }


    public void kakaoDisconnect(String accessToken) {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoLogoutRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v1/user/logout",
                HttpMethod.POST,
                kakaoLogoutRequest,
                String.class
        );

        // responseBody에 있는 정보를 꺼냄
        String responseBody = response.getBody();
        System.out.println("kakaoDisconnect======>>>>"+responseBody);
        /*
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        Long id = jsonNode.get("id").asLong();
        System.out.println("반환된 id: "+id);

        */
    }
}
