package com.zzimple.staff.dto.response;

import com.zzimple.staff.entity.StaffTimeOff;
import com.zzimple.staff.enums.Status;
import com.zzimple.staff.enums.TimeOffType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StaffTimeOffResponse {
  private Long staffTimeOffId;
  private String staffName;
  private Status status;
  private LocalDate startDate;
  private LocalDate endDate;
  private TimeOffType type;
  private String reason;

  public static StaffTimeOffResponse from(StaffTimeOff entity) {
    return StaffTimeOffResponse.builder()
        .staffTimeOffId(entity.getStaffTimeOffId())
        .staffName(entity.getStaffName())
        .status(entity.getStatus())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .type(entity.getType())
        .reason(entity.getReason())
        .build();
  }

}
