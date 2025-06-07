package com.zzimple.user.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.user.dto.request.UserLoginIdCheckRequest;
import com.zzimple.user.dto.request.LoginRequest;
import com.zzimple.user.dto.request.UserSignUpRequest;
import com.zzimple.user.dto.response.LoginIdCheckResponse;
import com.zzimple.user.dto.response.LoginResponse;
import com.zzimple.user.dto.response.SignUpResponse;
import com.zzimple.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {
  private final UserService userService;

  @Operation(
      summary = "[ 일반 사용자 | 토큰 X | 로그인 중복 검사 ]",
      description =
          """
           **Parameters**  \n
           loginId: 중복 검사할 로그인 아이디  \n

           **Returns**  \n
           isDuplicate: 중복 여부 (true/false)  \n
           message: 결과 메시지  \n
           """)
  @PostMapping("/login-id-duplicate-check")
  public ResponseEntity<BaseResponse<LoginIdCheckResponse>> checkLoginIdDuplicate(@RequestBody @Valid UserLoginIdCheckRequest request) {

    LoginIdCheckResponse result = userService.checkLoginIdDuplicate(request);

    String message = result.isDuplicate() ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";

    return ResponseEntity.ok(BaseResponse.success(message, result));
  }

  @Operation(
      summary = "[ 일반 사용자 | 토큰 X | 회원가입 ]",
      description =
          """
           **Parameters**  \n
           loginId: 로그인 아이디  \n
           password: 비밀번호  \n
           userName: 사용자 이름  \n
           phoneNumber: 전화번호  \n
           email: 이메일(선택) \n

           **Returns**  \n
           message: 결과 메시지  \n
           isSuccess: 회원가입 성공 여부  \n
           """)
  @PostMapping("/register")
  public ResponseEntity<BaseResponse<SignUpResponse>> register(@RequestBody @Valid UserSignUpRequest request) {
    SignUpResponse result = userService.registerUser(request);
    return ResponseEntity.ok(BaseResponse.success("회원가입이 완료되었습니다.", result));
  }

  @Operation(
      summary = "[ 일반 사용자 / 사장님 / 직원 | 토큰 X | 로그인 ]",
      description =
          """
          **Parameters**  \n
          loginId: 로그인 아이디  \n
          password: 비밀번호  \n

          **Returns**  \n
          accessToken: JWT 액세스 토큰  \n
          refreshToken: JWT 리프레시 토큰  \n
          message: 결과 메시지  \n
          isSuccess: 로그인 성공 여부  \n
          """)
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response ) {
    LoginResponse result = userService.login(request, response);
    return ResponseEntity.ok(BaseResponse.success("로그인에 성공하였습니다.", result));
  }
}