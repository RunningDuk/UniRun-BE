package com.runningduk.unirun.api.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.runningduk.unirun.domain.model.KakaoLogoutModel;
import com.runningduk.unirun.domain.repository.UserMapper;
import com.runningduk.unirun.domain.model.UserModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

    @Value("${kakao.logout-redirect-uri}")
    private String logoutRedirectUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    public UserModel getKakaoId(String code, HttpServletRequest request){
        HttpSession session = request.getSession();

        // 1. 토큰 받기
        String accessToken = getKakaoAccessToken(code);

        // 2. userId
        String[] returnValues = getKakaoUserId(accessToken);

        System.out.println("accessToken = " + accessToken);

        UserModel userModel = userMapper.selectUser(returnValues[0]);
        session.setAttribute("accessToken",accessToken);
        if(userModel == null){
            userModel = new UserModel();
            userModel.setUserId(returnValues[0]);
            userModel.setNickname(returnValues[1]);
            System.out.println("미등록");
        }else{
            System.out.println("등록");
            session.setAttribute("userId",userModel.getUserId());
        }
        return userModel;
    }

    public int kakaoLogout(HttpServletRequest request){
        HttpSession session = request.getSession();

        String accessToken = (String) session.getAttribute("accessToken");
        System.out.println("logout accessToken==>{}"+accessToken);
        int reutnrValue = 1;
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
        session.invalidate();
        // responseBody에 있는 정보를 꺼냄
        //String responseBody = response.getBody();
        System.out.println("kakaoDisconnect======>>>>"+response);
        return reutnrValue;
    }


    public KakaoLogoutModel getKakaLogOutInfo(){
        KakaoLogoutModel kakaoLogOutModel = new KakaoLogoutModel();
        kakaoLogOutModel.setClientId(kakaoClientId);
        kakaoLogOutModel.setLogoutUri(logoutUri);
        kakaoLogOutModel.setLogoutRedirectUri(logoutRedirectUri);
        return kakaoLogOutModel;
    }

    private String getKakaoAccessToken(String code) {
        // POST방식으로 key=value 데이터 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers  = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        log.info("kakaoClientSecret===>>>",kakaoClientSecret);
        // HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret",kakaoClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // HttpHeader와 HttpBody를 하나의 오브젝트로 담는다
        HttpEntity<MultiValueMap<String,String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // 실제요청
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // 응답 정보 출력
        System.out.println("response : ");
        System.out.println(response);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response.getBody().toString());

        String accessToken = element.getAsJsonObject().get("access_token").getAsString();
        String refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

        return accessToken;
    }

    private String[] getKakaoUserId(String accessToken) {

        String[] returnValues = new String[2];

        ///유저정보 요청
        RestTemplate restTemplate = new RestTemplate();

        //HttpHeader
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //HttpHeader와 HttpBody 담기
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST, kakaoProfileRequest, String.class);
        System.out.println("response : "+response.getBody().toString());
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response.getBody().toString());
        String userId = element.getAsJsonObject().get("id").getAsString();

        JsonElement element2 = element.getAsJsonObject().get("properties");
        String nickname = element2.getAsJsonObject().get("nickname").getAsString();

        returnValues[0] = userId;
        returnValues[1] = nickname;

        return returnValues;
    }
    public UserModel selectUser(String userId){ return userMapper.selectUser(userId); }
    public int insertUser(UserModel userModel){
        return userMapper.insertUser(userModel);
    }
    public int updateUser(UserModel userModel){
        return userMapper.updateUser(userModel);
    }
    public int deleteUser(String userId){
        return userMapper.deleteUser(userId);
    }


    public void kakaoDisconnect(String accessToken){
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
