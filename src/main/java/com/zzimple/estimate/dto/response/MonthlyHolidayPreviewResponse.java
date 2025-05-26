package com.zzimple.estimate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "공휴일 프리뷰 응답")
public class MonthlyHolidayPreviewResponse {

  @Schema(description = "날짜(yyyyMMdd)", example = "20250506")
  private String date;

  @Schema(description = "공휴일 여부 (Y/N)", example = "Y")
  private String holiday;

  @Schema(description = "공휴일 이름 (없으면 null)", example = "어린이날")
  private String dateName;

}
