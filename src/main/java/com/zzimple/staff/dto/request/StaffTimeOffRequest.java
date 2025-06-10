package com.zzimple.staff.dto.request;

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
  private String reason;
}
