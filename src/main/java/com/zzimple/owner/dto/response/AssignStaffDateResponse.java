package com.zzimple.owner.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignStaffDateResponse {
  private Long staffId;
  private String staffName;
  private String workDate;
}
