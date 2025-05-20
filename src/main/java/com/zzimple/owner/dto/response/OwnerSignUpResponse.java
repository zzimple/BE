package com.zzimple.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "게스트 회원가입 Response")
public class OwnerSignUpResponse {

  @Schema(description = "회원가입 결과", example = "true")
  private boolean isSuccess;
}
