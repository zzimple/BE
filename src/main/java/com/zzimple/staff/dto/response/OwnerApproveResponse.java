package com.zzimple.staff.dto.response;

import com.zzimple.staff.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerApproveResponse {
  @Schema(description = "승인 여부", example = "APPROVED")
  private Status status;
}