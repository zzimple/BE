package com.zzimple.estimate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "출발지/도착지 - 묶음 Response")
public class HolidayCheckResponse {

  @Schema(description = "공휴일 여부", example = "Y")
  private String isHoliday;

  @Schema(description = "공휴일 이름 (없으면 null)", example = "어린이날")
  private String dateName;

}
