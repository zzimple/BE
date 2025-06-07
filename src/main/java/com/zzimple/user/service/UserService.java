package com.zzimple.user.service;

import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.exception.OwnerErrorCode;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.user.enums.UserRole;
import com.zzimple.user.exception.UserErrorCode;
import com.zzimple.global.jwt.JwtUtil;
import com.zzimple.global.sms.service.SmsService;
import com.zzimple.user.dto.request.UserLoginIdCheckRequest;
import com.zzimple.user.dto.request.LoginRequest;
import com.zzimple.user.dto.request.UserSignUpRequest;
import com.zzimple.user.dto.response.LoginIdCheckResponse;
import com.zzimple.user.dto.response.LoginResponse;
import com.zzimple.user.dto.response.SignUpResponse;
import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final SmsService smsService;
  private final OwnerRepository ownerRepository;
  private final StoreRepository storeRepository;

  public LoginIdCheckResponse checkLoginIdDuplicate(UserLoginIdCheckRequest request) {
    // 로그인 아이디(이메일) 존재 여부 확인
    boolean isDuplicate = userRepository.findByLoginId(request.getLoginId()).isPresent();

    // 응답 생성
    return LoginIdCheckResponse.builder()
        .isDuplicate(isDuplicate)
        .build();
  }

  @Transactional
  public SignUpResponse registerUser(UserSignUpRequest request) {

    // 0. 휴대폰 인증 검사
    smsService.verifyPhoneCertified(request.getPhoneNumber());

    // 1. 중복 회원 검사
    if (userRepository.findByLoginId(request.getLoginId()).isPresent()) {
      log.warn("[회원가입 실패] 이미 존재하는 아이디 - ID: {}", request.getLoginId());
      throw new CustomException(UserErrorCode.DUPLICATE_LOGIN_ID);
    }

    try {
      // 필수 필드 유효성 검증
      if (!StringUtils.hasText(request.getLoginId())) {
        throw new CustomException(UserErrorCode.INVALID_INPUT);
      }
      if (!StringUtils.hasText(request.getPhoneNumber())) {
        throw new CustomException(UserErrorCode.INVALID_INPUT);
      }
      // 2. 비밀번호 암호화
      String encodedPassword = passwordEncoder.encode(request.getPassword());

      String email = StringUtils.hasText(request.getEmail()) ? request.getEmail() : null;

      // 3. 사용자 엔티티 생성
      User user =
          User.builder()
              .userName(request.getUserName())
              .phoneNumber(request.getPhoneNumber())
              .loginId(request.getLoginId())
              .password(encodedPassword)
              .email(email)
              .role(request.getUserRole())
              .build();

      // 4. 데이터베이스에 저장
      userRepository.save(user);

      log.info("[회원가입 성공] ID: {}, 이름: {}", user.getLoginId(), user.getUserName());

      // redis 키 지우기
      smsService.removeCertifiedPhoneKey(request.getPhoneNumber());

      return SignUpResponse.builder()
          .isSuccess(true)
          .build();
    } catch (Exception e) {
      log.error("[회원가입 오류] ID: {}, 이유: {}", request.getLoginId(), e.getMessage());
      throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 로그인
  @Transactional
  public LoginResponse login(LoginRequest request, HttpServletResponse response) {
    try {
      // 1. 로그인 아이디로 사용자 찾기
      User user = userRepository.findByLoginId(request.getLoginId())
          .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

      // 2. 비밀번호 확인
      if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new CustomException(UserErrorCode.INVALID_PASSWORD);
      }

      List<String> roles = List.of(user.getRole().name());
      String accessToken;
      Long storeId = null;

      // 사장님일 경우에만 storeId 추출
      if (user.getRole() == UserRole.OWNER) {
        Owner owner = ownerRepository.findByUserId(user.getId())
            .orElseThrow(() -> new CustomException(OwnerErrorCode.OWNER_NOT_FOUND));

        Store store = storeRepository.findByOwnerId(owner.getId())
            .orElseThrow(() -> new CustomException(OwnerErrorCode.STORE_NOT_FOUND));

        storeId = store.getId();
      }

      // 3. 토큰 생성
      accessToken = (storeId != null)
          ? jwtUtil.createAccessToken(user.getLoginId(), roles, storeId)
          : jwtUtil.createAccessToken(user.getLoginId(), roles); // 오버로드 버전 필요

      String refreshToken = jwtUtil.createRefreshToken(user.getLoginId());

      user.setRefreshToken(refreshToken);
      userRepository.save(user);

      // 7. HttpOnly 쿠키로 리프레시 토큰 내려주기
      long expirySeconds = jwtUtil.getRefreshTokenExpirySeconds();
      // (JwtUtil에 리프레시 토큰 만료 기간을 초 단위로 반환하는 메서드가 있다고 가정)
      ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
          .httpOnly(true)
          .secure(false) // 배포 시 true로 변경 (HTTPS)
          .sameSite("Lax")
          .path("/")
          .maxAge(Duration.ofSeconds(expirySeconds))
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

      // 5. 로그인 성공 응답 생성
      log.info("[로그인 성공] ID: {}, 이름: {}", user.getLoginId(), user.getUserName());

      return LoginResponse.builder()
          .accessToken(accessToken)
          .build();
    } catch (CustomException e) {
      log.warn("[로그인 실패 - CustomException] ID: {}, 이유: {}", request.getLoginId(), e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("[로그인 실패 - 서버 오류] ID: {}, 에러: {}", request.getLoginId(), e.getMessage(), e);
      throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
