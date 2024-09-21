package com.runningduk.unirun.config;

import com.runningduk.unirun.api.controller.GpsController;
import com.runningduk.unirun.api.service.GPSService;
import com.runningduk.unirun.api.intercepter.HttpSessionHandshakeIntercepter;
import com.runningduk.unirun.api.service.GpsScheduler;
import com.runningduk.unirun.api.service.RunningDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final GpsScheduler gpsScheduler;
    private final GPSService gpsService;
    private final RunningDataService runningDataService;

    @Bean
    public GpsController gpsController() {
        return new GpsController(gpsScheduler, gpsService, runningDataService);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gpsController(), "/running")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeIntercepter());
    }
}
