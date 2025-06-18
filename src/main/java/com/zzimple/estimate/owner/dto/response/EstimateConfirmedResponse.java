package com.zzimple.estimate.owner.dto.response;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.EstimateStatus;
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
@Schema(description = "견적서 최종 컨펌 응답")
public class EstimateConfirmedResponse {

  private Long estimateNo;

  private String roadFullAddr1;
  private String roadFullAddr2;

  private String moveDate;

  private String moveType;
  private String optionType;

  public static EstimateConfirmedResponse from(Estimate estimate) {

    return EstimateConfirmedResponse.builder()
        .estimateNo(estimate.getEstimateNo())
        .roadFullAddr1(estimate.getFromAddress().getRoadFullAddr())
        .roadFullAddr2(estimate.getToAddress().getRoadFullAddr())
        .moveDate(estimate.getMoveDate())
        .moveType(estimate.getMoveType() != null ? estimate.getMoveType().name() : null)
        .optionType(estimate.getOptionType() != null ? estimate.getOptionType().name() : null)
        .build();
  }
}