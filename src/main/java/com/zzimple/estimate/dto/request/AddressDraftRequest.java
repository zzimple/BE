package com.zzimple.estimate.dto.request;

import com.zzimple.estimate.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "출발지/도착지 - 단일 Request")
public class AddressDraftRequest {
  @Schema(description = "전체 도로명 주소", example = "서울특별시 강남구 테헤란로 223, 20층 (역삼동)")
  private String roadFullAddr;

  @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 223")
  private String roadAddr;

  @Schema(description = "상세 주소", example = "20층")
  private String addrDetail;

  @Schema(description = "우편번호", example = "06142")
  private String zipNo;

  @Schema(description = "건물 관리 번호", example = "1168010100106770025026369")
  private String buldMgtNo;

  // 건물 관리 번호 나눠주는 것 -> 엘베 API 활용할 때 쓰기 위해.
  public Address toEntity() {
    String no1 = buldMgtNo.substring(0, 3);
    String no2 = buldMgtNo.substring(3);

    return new Address(
        roadFullAddr,
        roadAddr,
        zipNo,
        addrDetail,
        no1,
        no2
    );
  }
}