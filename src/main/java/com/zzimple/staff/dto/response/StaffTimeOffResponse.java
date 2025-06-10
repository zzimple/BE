package com.zzimple.staff.dto.response;

import com.zzimple.staff.enums.Status;
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
  private String reason;
}
