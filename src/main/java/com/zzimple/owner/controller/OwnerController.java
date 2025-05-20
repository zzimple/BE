package com.zzimple.owner.controller;

import com.zzimple.global.common.response.ApiResponse;
import com.zzimple.owner.dto.request.OwnerLoginIdCheckRequest;
import com.zzimple.owner.dto.request.OwnerSignUpRequest;
import com.zzimple.owner.dto.response.OwnerLoginIdCheckResponse;
import com.zzimple.owner.dto.response.OwnerSignUpResponse;
import com.zzimple.owner.service.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor

public class OwnerController {

  private final OwnerService ownerService;

  @Operation(
      summary = "[ 사장님 | 토큰 X | 로그인 중복 검사 ]",
      description =
          """
              **Parameters**  \n
              loginId: 중복 검사할 로그인 아이디  \n
              
              **Returns**  \n
              isDuplicate: 중복 여부 (true/false)  \n
              message: 결과 메시지  \n
              """)
  @PostMapping("/login-id-duplicate-check")
  public ResponseEntity<ApiResponse<OwnerLoginIdCheckResponse>> checkLoginIdDuplicate(@RequestBody OwnerLoginIdCheckRequest request) {

    OwnerLoginIdCheckResponse result = ownerService.checkLoginIdDuplicate(request);
    String message = result.isDuplicate() ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";

    return ResponseEntity.ok(ApiResponse.success(message, result));
  }

  @Operation(
      summary = "[ 사장님 | 토큰 X | 회원가입 ]",
      description =
          """
              **Parameters**  \n
              b_no: 로그인 아이디  \n
              password: 비밀번호  \n
              userName: 사용자 이름  \n
              phoneNumber: 전화번호  \n
              email: 이메일(선택) \n
              insured : 보험가입 여부 \n
              
              **Returns**  \n
              message: 결과 메시지  \n
              isSuccess: 회원가입 성공 여부  \n
              """)
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<OwnerSignUpResponse>> register(
      @RequestBody OwnerSignUpRequest request) {
    OwnerSignUpResponse result = ownerService.registerOwner(request);
    return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", result));
  }
}
