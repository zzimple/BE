package com.zzimple.global.sms.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 휴대폰 인증번호 검증 요청")
public class VerifySignupCodeRequest {

  @NotBlank
  @Schema(description = "인증할 휴대폰 번호", example = "010-1234-5678", required = true)
  private String phone;

  @NotBlank
  @Schema(description = "전송받은 인증 번호", example = "123456", required = true)
  private String code;
}