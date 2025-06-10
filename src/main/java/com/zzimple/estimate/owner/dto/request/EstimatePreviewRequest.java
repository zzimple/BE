package com.zzimple.estimate.owner.dto.request;

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
@Schema(description = "견적서 목록 조회 요청")
public class EstimatePreviewRequest {

  @Schema(description = "이사 예정 연도 (moveDate를 분리한 값)", example = "2025")
  private Integer moveYear;

  @Schema(description = "이사 예정 월 (moveDate를 분리한 값)", example = "5")
  private Integer moveMonth;

  @Schema(description = "이사 예정 일 (moveDate를 분리한 값)", example = "6")
  private Integer moveDay;

  @Schema(description = "이사 유형 (SMALL 또는 HOME)", example = "SMALL")
  private String moveType;

  @Schema(description = "이사 옵션 (BASIC/PACKAGING/SEMIPACKAGING)", example = "BASIC")
  private String moveOption;

  @Schema(description = "출발지 시·도", example = "서울")
  private String fromRegion1;

  @Schema(description = "출발지 구·군", example = "강남구")
  private String fromRegion2;

  @Schema(description = "도착지 시·도", example = "경기")
  private String toRegion1;

  @Schema(description = "도착지 구·군", example = "성남시")
  private String toRegion2;

  @Schema(description = "견적서 상태", example = "PENDING")
  private EstimateStatus status;

}