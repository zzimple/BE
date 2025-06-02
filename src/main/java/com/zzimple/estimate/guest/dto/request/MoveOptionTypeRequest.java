package com.zzimple.estimate.guest.dto.request;

import com.zzimple.estimate.guest.enums.MoveOptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "소형 이사의 옵션 선택 요청")
public class MoveOptionTypeRequest {
  @Schema(description = "이사 옵션 타입", example = "BASIC")
  private MoveOptionType optionType;
}
