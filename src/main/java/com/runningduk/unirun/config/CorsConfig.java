package com.runningduk.unirun.config;

import com.runningduk.unirun.api.intercepter.LoginCheckInterceptor;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

/*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedMethods("*")
                .allowedOrigins("http://localhost:8888")
                .allowCredentials(true);
    }
 */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:8888");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(6000L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> filterBean = new FilterRegistrationBean<>(new CorsFilter(source));
        filterBean.setOrder(0);
        return filterBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

//        registry.addInterceptor(new LoginCheckInterceptor()) //LoginCheckInterceptor 등록
//                .order(1)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/", "/user/auth", "/user/register", "/calendar/running-schedules/monthly", "/calendar/running-schedules/daily");
    }

}