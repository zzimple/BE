package com.zzimple.estimate.owner.dto.request;

import com.zzimple.estimate.guest.enums.EstimateStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EstimateRespondRequest {
  @NotNull
  private Long storeId;
  @NotNull
  private EstimateStatus status; // ACCEPTED or REJECTED
}
