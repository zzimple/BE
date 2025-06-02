package com.zzimple.estimate.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "물품 기본 금액 저장 응답")
public class SaveItemBasePriceResponse {
  @Schema(description = "짐 종류 ID", example = "1001")
  private Long itemTypeId;

  @Schema(description = "기본 금액", example = "80000")
  private int basePrice;
}
