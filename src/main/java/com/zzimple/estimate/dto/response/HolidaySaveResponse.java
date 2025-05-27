package com.zzimple.estimate.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  @Schema(description = "공휴일 이름 (없으면 null)", example = "어린이날")
  private String dateName;

  public LocalDateTime getScheduledAt() {
    return LocalDateTime.parse(movedate + "T" + moveTime + ":00", DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss"));
  }
}
