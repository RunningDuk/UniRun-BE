package com.runningduk.unirun.api.intercepter;

import com.runningduk.unirun.api.response.CommonApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestURI = request.getRequestURI();
        log.info("[interceptor] : " + requestURI);
        HttpSession session = request.getSession(false);

        if(session == null || session.getAttribute("userId") == null) {
            // 로그인 되지 않음
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            CommonApiResponse<Object> responseBody = CommonApiResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Access Denied: Login required.")
                    .data(null)
                    .build();

            // 응답을 JSON 형식으로 변환하여 출력
            PrintWriter out = response.getWriter();
            out.print("{\"statusCode\":" + responseBody.getStatusCode() + ","
                    + "\"message\":\"" + responseBody.getMessage() + "\","
                    + "\"data\":null,"
                    + "\"sendTime\":\"" + responseBody.getSendTime() + "\"}");

            out.flush();

            return false;
        }

        // 로그인 되어있을 때
        return true;
    }
}
