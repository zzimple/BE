package com.zzimple.staff.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사장님 요청 Request")
public class StaffsendApprovalRequest {

  @Schema(description = "사장님 전화번호", example = "010-0000-0000")
  private String ownerPhoneNumber;
}
