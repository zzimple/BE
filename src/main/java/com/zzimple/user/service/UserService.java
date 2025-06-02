package com.zzimple.user.service;

import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.global.exception.UserErrorCode;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

      // ✅ 추가 - 필수 필드 유효성 검증
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
  public LoginResponse login(LoginRequest request) {
    try {
      // 1. 로그인 아이디로 사용자 찾기
      User user = userRepository.findByLoginId(request.getLoginId())
          .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

      // 2. 비밀번호 확인
      if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new CustomException(UserErrorCode.INVALID_PASSWORD);
      }

      // 3. 토큰 생성
      String accessToken = jwtUtil.createAccessToken(
          user.getLoginId(),
          List.of(user.getRole().name()));
      String refreshToken = jwtUtil.createRefreshToken(
          user.getLoginId());

      // 4. Refresh token 저장
      user.setRefreshToken(refreshToken);
      userRepository.save(user);

      // 5. 로그인 성공 응답 생성
      log.info("[로그인 성공] ID: {}, 이름: {}", user.getLoginId(), user.getUserName());

      return LoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
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
