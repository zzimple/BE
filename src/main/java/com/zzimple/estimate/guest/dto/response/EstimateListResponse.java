package com.zzimple.estimate.guest.dto.response;

import com.zzimple.estimate.guest.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "손님 견적서 목록 응답")
public class EstimateListResponse {

  @Schema(description = "견적서 번호", example = "1")
  private Long estimateNo;
//
//  @Schema(description = "이사 날짜", example = "6/6 (금)")
//  private String moveDate;
//
//  @Schema(description = "이사 시간", example = "오전 9:00")
//  private String moveTime;
//
//  @Schema(description = "출발지 주소", example = "서울특별시 서대문구 서경로 124 서경대학교 북악관 211호")
//  private Address fromAddress;
//
//  @Schema(description = "도착지 주소", example = "서울특별시 강남구 테헤란로 223 큰길타워빌딩 10층")
//  private Address toAddress;
//
//  @Schema(description = "가구 항목 개수", example = "3")
//  private int furnitureCount;
//
//  @Schema(description = "가전 항목 개수", example = "2")
//  private int applianceCount;

  private String storeName;    // 사장님 가게 이름
  private Integer truckCount;      // 트럭 개수
  private Integer totalPrice;     // 총 가격
}
