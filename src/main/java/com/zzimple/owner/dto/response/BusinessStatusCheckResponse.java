package com.zzimple.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessStatusCheckResponse {
  @Schema(description = "유효한 계속사업자인지 여부", example = "true")
  private boolean isValid;

  @Schema(description = "응답 메세지", example = "유효한 계속사업자입니다.")
  private String message;
}
