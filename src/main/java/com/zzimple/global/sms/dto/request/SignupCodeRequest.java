package com.zzimple.global.sms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메세지 보내기 Request")
public class SignupCodeRequest {
  @NotBlank
  @Schema(description = "수신번호", example = "010-0000-0000")
  private String phone;
}
