package com.zzimple.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerLoginIdCheckResponse {
  @Schema(description = "아이디 중복 검사 결과", example = "true")
  private boolean isDuplicate;
}