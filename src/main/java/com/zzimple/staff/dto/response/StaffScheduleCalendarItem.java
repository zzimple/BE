// StaffScheduleCalendarItem.java
package com.zzimple.staff.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zzimple.staff.enums.TimeOffType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StaffScheduleCalendarItem {
  private LocalDate date;
  private String type; // "WORK" or "TIME_OFF"

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private WorkInfo work;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TimeOffInfo timeOff;

  @Getter
  @Builder
  public static class WorkInfo {
    private Long estimateNo;
    private Long storeId;
    private String staffName;
    private String fromAddress; // 출발지 주소
    private String toAddress;   // 도착지 주소
  }

  @Getter
  @Builder
  public static class TimeOffInfo {
    private TimeOffType type;
    private String reason;
  }
}
