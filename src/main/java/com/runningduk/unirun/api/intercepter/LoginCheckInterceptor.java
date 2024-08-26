package com.runningduk.unirun.api.intercepter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
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
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter out = response.getWriter();
            out.print("{\"result\":\"No Session Id!\"}");

            return false;
        }
        // 로그인 되어있을 떄
        return true;
    }
}
