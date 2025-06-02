package com.zzimple.estimate.guest.dto.response;

import com.zzimple.estimate.guest.dto.request.AddressWithDetailRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "주소 조회용 응답")
public class AddressFullResponse {

  @Schema(description = "출발지 주소 정보")
  private AddressWithDetailRequest fromAddress;

  @Schema(description = "도착지 주소 정보")
  private AddressWithDetailRequest toAddress;
}
