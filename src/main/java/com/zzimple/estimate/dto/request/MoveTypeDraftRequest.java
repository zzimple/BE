package com.zzimple.estimate.dto.request;

import com.zzimple.estimate.enums.MoveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이사 유형 선택 요청")
public class MoveTypeDraftRequest {

  @Schema(description = "이사 유형", example = "SMALL")
  private MoveType moveType;
}
