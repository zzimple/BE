package com.zzimple.staff.dto.request;

import com.zzimple.staff.enums.TimeOffType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffTimeOffRequest {
  private LocalDate startDate;
  private LocalDate endDate;
  private TimeOffType type;
  private String reason;
}
