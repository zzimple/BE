package com.zzimple.estimate.guest.dto.response;

import com.zzimple.estimate.guest.enums.EstimateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GuestEstimateRespondResult {
  private Long estimateNo;
  private Long storeId;
  private EstimateStatus status;
}
