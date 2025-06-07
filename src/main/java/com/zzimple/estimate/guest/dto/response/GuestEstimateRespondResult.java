package com.zzimple.estimate.guest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GuestEstimateRespondResult {
  private Long estimateNo;
  private boolean accepted;
}
