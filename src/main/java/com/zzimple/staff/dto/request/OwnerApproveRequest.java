package com.zzimple.staff.dto.request;

import com.zzimple.staff.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사장님 직원 요청 받기 Request")
public class OwnerApproveRequest {

  @Schema(description = "직원 (아이디)", example = "1")
  private Long staffId;

  @Schema(description = "승인 여부", example = "false")
  private Status status;
}
