package com.zzimple.estimate.guest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "출발지/도착지 - 묶음 요청")
public class AddressDraftSaveRequest {
  @Schema(description = "현 집 주소 + 상세 정보")
  private AddressWithDetailRequest fromAddress;

  @Schema(description = "이사할 곳 집 주소 + 상세 정보")
  private AddressWithDetailRequest toAddress;
}
