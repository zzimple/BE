package com.zzimple.estimate.guest.dto.response;

import com.zzimple.estimate.guest.enums.MoveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이사 유형 저장 응답")
public class MoveTypeResponse {

  @Schema(description = "저장된 이사 유형", example = "HOME_MOVE")
  private MoveType moveType;

}