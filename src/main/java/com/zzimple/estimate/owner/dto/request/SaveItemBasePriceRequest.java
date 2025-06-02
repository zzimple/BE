package com.zzimple.estimate.owner.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "물품 기본 금액 저장 요청")
public class SaveItemBasePriceRequest {

  @Schema(description = "짐 종류 ID", example = "1001")
  private Long itemTypeId;

  @Schema(description = "기본 금액", example = "80000")
  private int basePrice;
}