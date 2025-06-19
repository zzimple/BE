package com.zzimple.global.jwt;

import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws IOException {
    try {
      log.debug("요청 URI: {}, Method: {}", request.getRequestURI(), request.getMethod());

//       추가
    String jwt = null;
    if (request.getCookies() != null) {
      for (var cookie : request.getCookies()) {
        log.debug("쿠키 확인 - name: {}, value: {}", cookie.getName(), cookie.getValue());

        if ("accessToken".equals(cookie.getName())) {
          jwt = cookie.getValue();
          log.debug("accessToken 쿠키에서 JWT 추출: {}", jwt);

          break;
        }
      }
    }
////       Authorization 헤더에서 JWT 토큰을 가져옴
//      final String authorizationHeader = request.getHeader("Authorization");

//       인증 헤더가 없는 경우 다음 필터로
//      if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//        jwt = authorizationHeader.substring(7);
//
//        filterChain.doFilter(request, response);
//        return;
//      }
////       추가
//      else {
//        // ✅ Authorization 헤더가 없으면 쿠키에서 accessToken 추출
//        if (request.getCookies() != null) {
//          for (var cookie : request.getCookies()) {
//            if ("accessToken".equals(cookie.getName())) {
//              jwt = cookie.getValue();
//              break;
//            }
//          }
//        }
//      }

      // ✅ 2. 토큰이 없는 경우 필터 체인 계속
      if (jwt == null) {
        log.debug("JWT 토큰이 존재하지 않습니다. 필터 체인 계속.");

        filterChain.doFilter(request, response);
        return;
      }

//      String jwt = authorizationHeader.substring(7);
//      String loginId = jwtUtil.extractLoginId(jwt);

//      // 토큰은 유효하지만 SecurityContext에 인증 정보가 없는 경우
//      if (loginId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//        User user =
//            userRepository
//                .findByLoginId(loginId)
//                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
//
//        if (jwtUtil.validateToken(jwt)) {
//          Long storeId = jwtUtil.extractStoreId(jwt);
//
//          try {
//            storeId = jwtUtil.extractStoreId(jwt);
//          } catch (Exception ex) {
//            log.debug("storeId는 토큰에 존재하지 않거나 null입니다. 일반 사용자로 간주.");
//          }

      // ✅ 3. 토큰으로부터 사용자 정보 추출 및 인증 처리
      String loginId = jwtUtil.extractLoginId(jwt);
      log.debug("JWT에서 추출한 loginId: {}", loginId);

      if (loginId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        log.debug("SecurityContext에 인증 정보가 없습니다. 사용자 인증 시도 중...");

        User user = userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        log.debug("사용자 정보 조회 완료: id={}, loginId={}", user.getId(), user.getLoginId());


        if (jwtUtil.validateToken(jwt)) {
          Long storeId = null;
          try {
            storeId = jwtUtil.extractStoreId(jwt);
            log.debug("JWT에서 추출한 storeId: {}", storeId);

          } catch (Exception ex) {
            log.debug("storeId는 토큰에 존재하지 않거나 null입니다. 일반 사용자로 간주.");
          }

          CustomUserDetails userDetails = new CustomUserDetails(user, storeId); // storeId 포함 생성자 사용

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);

          log.debug("Security Context에 '{}' 인증 정보를 저장했습니다", loginId);
        }
      }  else {
        log.warn("JWT 토큰 유효성 검증 실패 - loginId: {}", loginId);
      }

      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      log.warn("만료된 JWT 토큰입니다. URI: {}", request.getRequestURI());
      sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "만료된 토큰입니다.");
    } catch (SignatureException | MalformedJwtException e) {
      log.warn("유효하지 않은 JWT 토큰입니다. URI: {}", request.getRequestURI());
      sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
    } catch (UsernameNotFoundException e) {
      log.warn("존재하지 않는 사용자입니다. URI: {}", request.getRequestURI());
      sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } catch (ServletException e) {
      Throwable cause = e.getCause();
      handleException(cause, request, response);
    } catch (Exception e) {
      handleException(e, request, response);
    }
  }

  private void handleException(
      Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException {
    int statusCode;
    String message = e.getMessage();

    if (e instanceof IllegalArgumentException || e instanceof NoSuchElementException) {
      statusCode = HttpServletResponse.SC_NOT_FOUND; // 404
      if (message == null) message = "요청한 리소스를 찾을 수 없습니다.";
    } else if (e instanceof IllegalStateException) {
      statusCode = HttpServletResponse.SC_BAD_REQUEST; // 400
      if (message == null) message = "잘못된 요청입니다.";
    } else if (e instanceof AccessDeniedException) {
      statusCode = HttpServletResponse.SC_FORBIDDEN; // 403
      if (message == null) message = "접근 권한이 없습니다.";
    } else if (e instanceof AuthenticationException) {
      statusCode = HttpServletResponse.SC_UNAUTHORIZED; // 401
      if (message == null) message = "인증에 실패했습니다.";
    } else if (e instanceof ExpiredJwtException) {
      statusCode = HttpServletResponse.SC_UNAUTHORIZED; // 401
      message = "만료된 토큰입니다.";
    } else if (e instanceof SignatureException || e instanceof MalformedJwtException) {
      statusCode = HttpServletResponse.SC_UNAUTHORIZED; // 401
      message = "유효하지 않은 토큰입니다.";
    } else {
      statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // 500
      message = "서버 내부 오류가 발생했습니다.";
    }

    log.error(
        "필터 처리 중 예외 발생 - URI: {}, Method: {}, Error: {}, ErrorType: {}",
        request.getRequestURI(),
        request.getMethod(),
        e.getMessage(),
        e.getClass().getSimpleName());

    sendErrorResponse(response, statusCode, message);
  }

  private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
      throws IOException {
    response.setStatus(statusCode);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(String.format("{\"isSuccess\":false,\"message\":\"%s\"}", message));
  }

  // 특정 경로는 필터 적용 제외 (예: 로그인, 회원가입)
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth/")
        || path.equals("/juso/callback")
        || path.equals("/users/refresh-token");
  }
}