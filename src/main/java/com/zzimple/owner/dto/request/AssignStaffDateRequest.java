package com.zzimple.owner.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "직원 일 배정 요청")
public class AssignStaffDateRequest {
  private Long staffId;
  private LocalDate workDate;
}
