package com.zzimple.estimate.owner.dto.response;

import com.zzimple.estimate.guest.enums.EstimateStatus;
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
public class EstimateRespondResponse {
  private Long estimateNo;
  private Long ownerId;
  private EstimateStatus status;
}
