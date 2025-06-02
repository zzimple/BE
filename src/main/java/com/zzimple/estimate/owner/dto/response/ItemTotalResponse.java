package com.zzimple.estimate.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "각 이사 항목의 기본 금액, 추가금, 총 금액 정보를 담은 응답 DTO")
public class ItemTotalResponse {
  private Long itemId;
  private int baseTotal;
  private int extraTotal;
  private int itemTotal;
}
