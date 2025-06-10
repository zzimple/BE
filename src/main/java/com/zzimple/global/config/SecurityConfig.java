package com.zzimple.global.config;

import com.zzimple.global.jwt.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Order(1)
public class SecurityConfig {

  private final CorsConfig corsConfig;

  @Value("${swagger.auth.username}")
  private String swaggerUsername;

  @Value("${swagger.auth.password}")
  private String swaggerPassword;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
      JwtRequestFilter jwtRequestFilter) throws Exception {
    httpSecurity
        // CORS 설정
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

        // CSRF 비활성화 (JWT 기반 REST API의 일반적인 설정)
        .csrf(CsrfConfigurer::disable)

        // 세션 관리 - JWT 기반이므로 무상태
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        .httpBasic(Customizer.withDefaults())

        // JWT 인증 필터를 Spring Security 필터 체인에 등록 (UsernamePasswordAuthenticationFilter 앞에 실행)
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

        // 권한 설정
        .authorizeHttpRequests(auth -> auth
                // 정적 리소스 허용
                .requestMatchers(
                    "/favicon.ico", "/error"
                ).permitAll()

                // Swagger 리소스 개발자만 허용
                .requestMatchers(
                    "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
                ).hasRole("DEVELOPER")

                // 인증 관련 요청 허용
                .requestMatchers("/auth/**", "/oauth2/**").permitAll()

                .requestMatchers("/owner/**", "/users/**", "/sms/**").permitAll()


                // 개발 할 때만 풀어두기
//                .requestMatchers("/estimates/draft/**").permitAll()

                .requestMatchers("/juso/**").permitAll()


                // 직원 전용 API
                .requestMatchers("/staff/request", "/staff/time-off/request").hasRole("STAFF")

                // 사장 전용 API
                .requestMatchers("/staff/approve","/staff/list", "/estimates/owner/**", "/staff/time-off/pending", "/staff/time-off/decide/**", "/owner/schedule/**").hasRole("OWNER")

                // 고객 전용 API
                .requestMatchers("/estimates/draft/**", "/guest/my/**").hasRole("GUEST")

                .requestMatchers("/view/**").hasAnyRole("OWNER", "GUEST")

                // 그 외 요청 차단
                .anyRequest().denyAll()
        );

    return httpSecurity.build();
  }

  // 패스워드를 받고 자동으로 인코딩(암호화)할 수 있는 메소드
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails swaggerUser =
        User.builder()
            .username(swaggerUsername)
            .password(passwordEncoder().encode(swaggerPassword))
            .roles("DEVELOPER")
            .build();

    return new InMemoryUserDetailsManager(swaggerUser);
  }
}