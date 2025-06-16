package com.zzimple.estimate.owner.dto.response;

import com.zzimple.estimate.guest.enums.MoveItemCategory;
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
public class EstimateItemWithExtraChargeResponse {
  private Long estimateNo;
  private Long itemTypeId;
  private String itemTypeName;
  private MoveItemCategory moveItemCategory;
  private int basePrice;
  private int extraCharge;
  private String reason;
}
