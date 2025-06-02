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
@Schema(description = "사장님 - 공개 견적서 추가 입력 응답")
public class EstimateDetailUpdateResponse {

  private EstimatePreviewDetailResponse estimateDetail;
  private Integer truckCount;
  private String ownerMessage;

}
