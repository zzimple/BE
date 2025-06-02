package com.zzimple.estimate.owner.dto.request;

import com.zzimple.estimate.guest.enums.MoveItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "견적서 물품 가격 저장 요청")
public class SaveEstimatePriceRequest {

  @Schema(description = "짐 종류 ID", example = "1001")
  private Long itemTypeId;

  private String itemTypeName;


  private MoveItemCategory category;

  @Schema(description = "수량", example = "1")
  private int quantity;

  @Schema(description = "기본 금액", example = "80000")
  private int basePrice;

  @Schema(description = "추가금 목록")
  private List<ExtraChargeRequest> extraCharges;

  @Getter
  @NoArgsConstructor
  @Schema(description = "추가금 항목")
  public static class ExtraChargeRequest {

    @Schema(description = "추가 금액", example = "5000")
    private int amount;

    @Schema(description = "사유", example = "고층 계단 이동")
    private String reason;
  }
}
