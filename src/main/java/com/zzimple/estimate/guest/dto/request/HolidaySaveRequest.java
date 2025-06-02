package com.zzimple.estimate.guest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "이사 날짜 저장 요청")
public class HolidaySaveRequest {
  @Schema(description = "저장 할 날짜")
  private String date;

  @Schema(description = "저장 할 시간")
  private String time;
}
