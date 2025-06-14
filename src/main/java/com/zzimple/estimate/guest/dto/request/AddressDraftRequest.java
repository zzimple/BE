package com.zzimple.estimate.guest.dto.request;

import com.zzimple.estimate.guest.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "출발지/도착지 - 단일 요청")
public class AddressDraftRequest {
  @Schema(description = "전체 도로명 주소", example = "서울특별시 강남구 테헤란로 223, 20층 (역삼동)")
  private String roadFullAddr;

  @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 223")
  private String roadAddrPart1;

  @Schema(description = "상세 주소", example = "20층")
  private String addrDetail;

  @Schema(description = "우편번호", example = "06142")
  private String zipNo;

  @Schema(description = "X좌표")
  private String entX;

  @Schema(description = "Y좌표")
  private String entY;

  public Address toEntity() {
    return new Address(
        this.getRoadFullAddr(),
        this.getRoadAddrPart1(),
        this.getZipNo(),
        this.getAddrDetail(),
        this.getEntX(),
        this.getEntY()
    );
  }
}