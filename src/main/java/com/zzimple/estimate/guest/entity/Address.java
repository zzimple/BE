package com.zzimple.estimate.guest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Address {

  @Column(name = "road_full_addr", nullable = false)
  private String roadFullAddr;    // 전체 도로명 주소 (예: 서울 강남구 테헤란로 123)

  @Column(name = "road_addr", nullable = false)
  private String roadAddrPart1;        // 도로명 주소 요약 (예: 테헤란로)

  @Column(name = "zip_no", nullable = false)
  private String zipNo;         // 우편번호

  @Column(name = "addr_detail")
  private String addrDetail;  // 상세 주소 (예: 101동 203호)

  // 그냥 값 보관용 객체라서 생성자 씀.
  public Address(String roadFullAddr, String roadAddrPart1, String zipNo, String addrDetail) {
    this.roadFullAddr = roadFullAddr;
    this.roadAddrPart1 = roadAddrPart1;
    this.zipNo = zipNo;
    this.addrDetail = addrDetail;
  }
}