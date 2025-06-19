package com.zzimple.global.jwt;

import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.exception.OwnerErrorCode;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.enums.UserRole;
import com.zzimple.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User")
public class AuthController {
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final OwnerRepository ownerRepository;
  private final StoreRepository storeRepository;

  @Operation(
      summary = "토큰 필요 O 보낸 토큰이 만료되었을 경우 재발급",
      description =
          """
           **Returns**  \n
           accessToken: 새로발급된 accessToken  \n
           """)
  @PostMapping("/refresh-token")
  public ResponseEntity<?> refresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
    try {

//      System.out.println(refreshToken);

      // Bearer 검증
      if (refreshToken == null || refreshToken.isBlank()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Refresh Token이 필요합니다."));
      }

//      String token = refreshToken.substring(7);

      System.out.println("Raw refreshToken from Cookie: >" + refreshToken + "<");


      // Refresh Token에서 loginId 추출
      String loginId = jwtUtil.extractLoginId(refreshToken);

      if (loginId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("유효하지 않은 Refresh Token입니다."));
      }

      // refresh Token 만료 여부 확인
      if (jwtUtil.isTokenExpired(refreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Refresh Token이 만료되었습니다. 다시 로그인해주세요."));
      }

      // User 조회
      User user = userRepository
          .findByLoginId(loginId)
          .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));


      // 비교하기
      System.out.println(user.getRefreshToken());

      // 1) 양쪽 값에 구분자를 넣어서 찍어보기
      System.out.println(">"+ refreshToken +"<");
      System.out.println(">"+ user.getRefreshToken() +"<");

// 2) 길이 비교
      System.out.println(refreshToken.length() + " vs " + user.getRefreshToken().length());

// 3) 문자별 차이점 찾기
      int len = Math.min(refreshToken.length(), user.getRefreshToken().length());
      for (int i = 0; i < len; i++) {
        char a = refreshToken.charAt(i), b = user.getRefreshToken().charAt(i);
        if (a != b) {
          System.out.printf("idx %d: token='%c'(%d) vs db='%c'(%d)\n",
              i, a, (int)a, b, (int)b);
          break;
        }
      }


      if (!refreshToken.equals(user.getRefreshToken())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("변조된 Refresh Token입니다."));
      }

      System.out.println(user.getRefreshToken());


      List<String> roles = Collections.singletonList(user.getRole().name());
      String newAccessToken;

      if (user.getRole() == UserRole.OWNER) {
//        Owner owner = ownerRepository.findByUserId(user.getId())
//            .orElseThrow(() -> new CustomException(OwnerErrorCode.OWNER_NOT_FOUND));

//        Store store = storeRepository.findByOwnerUserId(owner.getId())
//            .orElseThrow(() -> new CustomException(OwnerErrorCode.STORE_NOT_FOUND));

        Store store = storeRepository.findByOwnerUserId(user.getId())
            .orElseThrow(() -> new CustomException(OwnerErrorCode.STORE_NOT_FOUND));

        Long storeId = store.getId();         // 가게 ID
        Long ownerId = store.getOwnerId();    // 사장님 PK
        newAccessToken = jwtUtil.createAccessToken(
            user.getLoginId(), roles, storeId, ownerId
        );

//        newAccessToken = jwtUtil.createAccessToken(loginId, roles, store.getId(), owner.getId() );
      } else {
        newAccessToken = jwtUtil.createAccessToken(loginId, roles); // 고침
      }

      // ✅ accessToken 쿠키 설정
      ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
          .httpOnly(false)
          .secure(false) // 배포 시 true (HTTPS)
          .sameSite("Lax")
          .path("/")
          .maxAge(Duration.ofHours(1))
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

      return ResponseEntity.ok().body(new TokenResponse(newAccessToken));

//      return ResponseEntity.ok(new TokenResponse(newAccessToken));
    }
    // refresh Token 파싱 실패 (만료된 토큰)d
    catch (ExpiredJwtException e) {
      log.error("만료된 Refresh Token입니다.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse("Refresh Token이 만료되었습니다. 다시 로그인해주세요."));

    } catch (Exception e) {
      log.error("Token 갱신 중 오류 발생: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("토큰 갱신 중 서버 오류가 발생했습니다. 잠시 후 다시시도해주세요."));
    }
  }

  @PostMapping("/logout")
  @Operation(summary = "로그아웃 (refreshToken 쿠키 + DB 삭제)")
  public ResponseEntity<Void> logout(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestHeader("Authorization") String accessTokenHeader
  ) {

    // ✅ 1. accessToken은 Filter에서 추출하거나 Cookie에서 읽도록
    String accessToken = jwtUtil.extractTokenFromRequest(request); // 커스텀 메서드 필요

    if (accessToken != null) {
      String loginId = jwtUtil.extractLoginId(accessToken);
      if (loginId != null) {
        userRepository.findByLoginId(loginId).ifPresent(user -> {
          user.setRefreshToken(null);
          userRepository.save(user);
        });
      }
    }

    // 1. 쿠키에서 refreshToken 삭제
    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // HTTPS 사용하는 경우
    cookie.setPath("/");
    cookie.setMaxAge(0); // 즉시 만료
    response.addCookie(cookie);

    Cookie accessCookie = new Cookie("accessToken", null);
    accessCookie.setHttpOnly(false); // accessToken은 HttpOnly가 아니었을 가능성도 고려
    accessCookie.setPath("/");
    accessCookie.setMaxAge(0);
    response.addCookie(accessCookie);

//    // 2. accessToken에서 loginId 추출
//    if (accessTokenHeader != null && accessTokenHeader.startsWith("Bearer ")) {
//      String accessToken = accessTokenHeader.substring(7);
//      String loginId = jwtUtil.extractLoginId(accessToken);
//
//      if (loginId != null) {
//        userRepository.findByLoginId(loginId).ifPresent(user -> {
//          user.setRefreshToken(null); // DB에서 refreshToken 제거
//          userRepository.save(user);
//        });
//      }
//    }

    return ResponseEntity.ok().build();
  }

}

@Getter
@AllArgsConstructor
class TokenResponse {
  private String accessToken;
}

@Getter
@AllArgsConstructor
class ErrorResponse {
  private String message;
}