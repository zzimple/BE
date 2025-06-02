package com.zzimple.estimate.guest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주소 + 건물 상세 정보 요청")
public class AddressWithDetailRequest {
  @Schema(description = "기본 주소")
  private AddressDraftRequest address;

  @Schema(description = "건물 상세 정보")
  private AddressDetailInfoRequest detailInfo;
}