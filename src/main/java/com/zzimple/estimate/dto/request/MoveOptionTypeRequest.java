package com.zzimple.estimate.dto.request;

import com.zzimple.estimate.enums.MoveOptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
@Schema(description = "소형 이사의 옵션 선택 요청")
public class MoveOptionTypeRequest {
  @Schema(description = "이사 옵션 타입", example = "BASIC")
  private MoveOptionType optionType;
}
