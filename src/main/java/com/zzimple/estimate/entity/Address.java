package com.zzimple.estimate.entity;

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
  private String roadAddr;        // 도로명 주소 요약 (예: 테헤란로)

  @Column(name = "zip_no", nullable = false)
  private String zipNo;         // 우편번호

  @Column(name = "addr_detail")
  private String addrDetail;  // 상세 주소 (예: 101동 203호)

  @Column(name = "buld_mgt_no1", nullable = false)
  private String buldMgtNo; // 건물관리번호 앞자리 (3자리, 예: 660)

  @Column(name = "buld_slno") // 건물 부번 (건물번호 뒷자리)
  private Number buldSlno;

  @Column(name = "emd_nm") // 읍면동명
  private String emdNm;

  @Column(name = "li_nm") // 법정리명
  private String liNm;

  // 그냥 값 보관용 객체라서 생성자 씀.
  public Address(String roadFullAddr, String roadAddr, String zipNo, String addrDetail,
      String buldMgtNo, Number buldSlno, String emdNm, String liNm) {
    this.roadFullAddr = roadFullAddr;
    this.roadAddr = roadAddr;
    this.zipNo = zipNo;
    this.addrDetail = addrDetail;
    this.buldMgtNo = buldMgtNo;
    this.buldSlno = buldSlno;
    this.emdNm = emdNm;
    this.liNm = liNm;
  }
}