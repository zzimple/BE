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
@Schema(description = "사장님 - 최종 계산 결과 응답")
public class CalculateOwnerInputResponse {
  private int itemTotal;
  private int truck_charge;
  private int extraTotal;
  private int finalTotal;
}
