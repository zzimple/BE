package com.zzimple.owner.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사업자등록 상태조회 요청 Request")
public class BusinessStatusRequest {
  @Schema(
      description = "사업자등록번호 리스트 (최대 100개)",
      example = "[\"1120486857\"]",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  private List<String> b_no;
}