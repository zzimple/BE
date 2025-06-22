package com.zzimple.estimate.owner.dto.response;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.owner.entity.EstimateResponse;
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
@Schema(description = "견적서 프리뷰 응답")
public class EstimatePreviewResponse {

  private Long estimateNo;

  // 출발지 시·도, 구·군
  private String fromRegion1;
  private String fromRegion2;

  // 도착지 시·도, 구·군
  private String toRegion1;
  private String toRegion2;

  private int moveYear;
  private int moveMonth;
  private int moveDay;

  private String moveType;
  private String optionType;

  private EstimateStatus status;
//  private EstimateResponse estimateResponse;

  private boolean respondedByMe;


  public static EstimatePreviewResponse fromEntity(Estimate estimate) {

    // 출발지
    String[] fromParts = estimate.getFromAddress().getRoadFullAddr().split(" ");
    String from1 = fromParts.length > 0 ? fromParts[0] : null;
    String from2 = fromParts.length > 1 ? fromParts[1] : null;

    // 도착지
    String[] toParts = estimate.getToAddress().getRoadFullAddr().split(" ");
    String to1 = toParts.length > 0 ? toParts[0] : null;
    String to2 = toParts.length > 1 ? toParts[1] : null;

    // 날짜 (rawDate 예: "20250506")
    String rawDate = estimate.getMoveDate();
    int year  = Integer.parseInt(rawDate.substring(0, 4));
    int month = Integer.parseInt(rawDate.substring(4, 6));
    int day   = Integer.parseInt(rawDate.substring(6, 8));

    return EstimatePreviewResponse.builder()
        .estimateNo(estimate.getEstimateNo())
        .fromRegion1(from1)
        .fromRegion2(from2)
        .toRegion1(to1)
        .toRegion2(to2)
        .moveYear(year)
        .moveMonth(month)
        .moveDay(day)
        .moveType(estimate.getMoveType()   != null ? estimate.getMoveType().name()   : null)
        .optionType(estimate.getOptionType() != null ? estimate.getOptionType().name() : null)
        .status(estimate.getStatus())
        .respondedByMe(false)
        .build();
  }
}