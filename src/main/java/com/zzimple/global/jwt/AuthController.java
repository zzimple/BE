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
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
  public ResponseEntity<?> refresh(@RequestHeader("Authorization") String refreshToken) {
    try {
      // Bearer 검증
      if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Refresh Token이 필요합니다."));
      }

      String token = refreshToken.substring(7);

      // Refresh Token에서 loginId 추출
      String loginId = jwtUtil.extractLoginId(token);

      if (loginId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("유효하지 않은 Refresh Token입니다."));
      }

      // refresh Token 만료 여부 확인
      if (jwtUtil.isTokenExpired(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Refresh Token이 만료되었습니다. 다시 로그인해주세요."));
      }

      // User 조회
      User user = userRepository
          .findByLoginId(loginId)
          .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

      if (!token.equals(user.getRefreshToken())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("변조된 Refresh Token입니다."));
      }

      List<String> roles = Collections.singletonList(user.getRole().name());
      String newAccessToken;

      if (user.getRole() == UserRole.OWNER) {
        Owner owner = ownerRepository.findByUserId(user.getId())
            .orElseThrow(() -> new CustomException(OwnerErrorCode.OWNER_NOT_FOUND));
        Store store = storeRepository.findByOwnerId(owner.getId())
            .orElseThrow(() -> new CustomException(OwnerErrorCode.STORE_NOT_FOUND));

        newAccessToken = jwtUtil.createAccessToken(loginId, roles, store.getId());
      } else {
        newAccessToken = jwtUtil.createAccessToken(loginId, roles); // 고침
      }

      return ResponseEntity.ok(new TokenResponse(newAccessToken));
    }
    // refresh Token 파싱 실패 (만료된 토큰)
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