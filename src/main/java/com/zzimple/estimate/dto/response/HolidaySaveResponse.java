package com.zzimple.estimate.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이사 날짜 저장 응답")
public class HolidaySaveResponse {
  @Schema(description = "이사 날짜", example = "20250506")
  @JsonFormat(pattern = "yyyyMMdd")
  private String movedate;

  @Schema(description = "이사 시간 (시:분)", example = "14:30")
  private String moveTime;
}
