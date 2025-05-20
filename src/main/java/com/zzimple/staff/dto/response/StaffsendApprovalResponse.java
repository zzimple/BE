package com.zzimple.staff.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaffsendApprovalResponse {
  @Schema(description = "승인 여부", example = "APPROVED")
  private boolean success;
}