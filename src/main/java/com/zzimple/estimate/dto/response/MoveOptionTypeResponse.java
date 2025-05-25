package com.zzimple.estimate.dto.response;

import com.zzimple.estimate.enums.MoveOptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이사 옵션 선택 요청")
public class MoveOptionTypeResponse {
  private MoveOptionType optionType;
}
