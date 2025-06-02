package com.zzimple.estimate.guest.dto.response;

import com.zzimple.estimate.guest.enums.MoveOptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이사 옵션 선택 응답")
public class MoveOptionTypeResponse {
  private MoveOptionType optionType;
}
