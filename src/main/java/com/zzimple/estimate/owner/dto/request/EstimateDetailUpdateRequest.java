package com.zzimple.estimate.owner.dto.request;

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
@Schema(description = "사장님 - 공개 견적서 추가 입력 요청")
public class EstimateDetailUpdateRequest {

  @Schema(description = "트럭 개수", example = "1")
  private Integer truckCount;

  @Schema(description = "사장님 추가 말", example = "어쩌고 저쩌고")
  private String ownerMessage;

}
