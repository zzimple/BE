package com.zzimple;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
public class JusoSecurityConfig {

  @Bean
  @Order(0)
  public SecurityFilterChain jusoSecurityChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/juso/**")             // /juso/** 로만 매칭
        .cors(AbstractHttpConfigurer::disable)   // CORS 필터 아예 비활성화
        .csrf(AbstractHttpConfigurer::disable)   // CSRF 비활성화
        .authorizeHttpRequests(a -> a
            .anyRequest().permitAll()              // 인증·인가 전부 OFF
        );
    return http.build();
  }
}
