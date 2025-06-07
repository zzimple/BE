package com.zzimple.estimate.owner.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사장님 마이페이지 기본 추가금 저장 요청")
public class SaveStorePriceSettingRequest {

  @Schema(description = "트럭 1대당 추가요금", example = "30000")
  private Integer perTruckCharge;

  @Schema(description = "공휴일 추가요금", example = "50000")
  private Integer holidayCharge;

  @Schema(description = "손 없는 날 추가요금", example = "40000")
  private Integer goodDayCharge;

  @Schema(description = "주말 추가요금", example = "30000")
  private Integer weekendCharge;
}
