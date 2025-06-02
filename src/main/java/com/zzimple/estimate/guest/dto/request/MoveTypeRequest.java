package com.zzimple.estimate.guest.dto.request;

import com.zzimple.estimate.guest.enums.MoveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이사 유형 선택 요청")
public class MoveTypeRequest {

  @Schema(description = "이사 유형", example = "SMALL")
  private MoveType moveType;
}
