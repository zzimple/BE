package com.zzimple.estimate.guest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "출발지/도착지 - 묶음 응답")
public class AddressDraftResponse {
  @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 223")
  private String roadAddr;
}
