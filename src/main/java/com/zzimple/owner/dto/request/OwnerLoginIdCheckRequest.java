package com.zzimple.owner.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OwnerLoginIdCheckRequest {
  @Schema(description = "아이디", example = "1234567890")
  private String loginId;
}