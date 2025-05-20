package com.zzimple.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
  @Schema(description = "아이디", example = "test01")
  private String loginId;

  @Schema(description = "비밀번호", example = "비밀번호 입력")
  private String password;
}