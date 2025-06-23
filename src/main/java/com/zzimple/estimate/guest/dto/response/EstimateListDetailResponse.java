package com.zzimple.estimate.guest.dto.response;


import com.zzimple.estimate.guest.entity.Address;
import com.zzimple.estimate.guest.entity.AddressDetailInfo;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import com.zzimple.estimate.owner.dto.response.MoveItemPreviewDetailResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
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
@Schema(description = "손님 견적서 상세보기 응답")
public class EstimateListDetailResponse {

  private Long estimateNo;
  private String storeName;
  private String ownerName;
  private String ownerPhone;

  private Long userId;
  private String moveDate;
  private LocalDateTime moveTime;
  private MoveType moveType;
  private MoveOptionType optionType;

  private Address fromAddress;
  private AddressDetailInfo fromDetailInfo;

  private Address toAddress;
  private AddressDetailInfo toDetailInfo;

  private String customerMemo;

  @Schema(description = "트럭 개수", example = "2")
  private Integer truckCount;

  @Schema(description = "트럭 총 요금 (트럭 단가 * 트럭 개수)", example = "100000")
  private Integer truckTotalPrice;


  @Schema(description = "사장님이 고객에게 전달할 메시지", example = "이사 당일 안전하게 도와드리겠습니다!")
  private String ownerMessage;

  @Schema(description = "물품별 단가 및 추가금 리스트")
  private List<SaveEstimatePriceRequest> itemPriceDetails;

  @Schema(description = "기타 추가금 항목 리스트 (사유 + 금액)")
  private List<SaveEstimatePriceRequest.ExtraChargeRequest> extraCharges;

  private List<MoveItemPreviewDetailResponse> items;

  private Integer totalPrice;

  @Schema(description = "공휴일 추가금 (isHoliday=true일 때만 세팅)")
  private Integer holidayCharge;

  @Schema(description = "손 없는 날 추가금 (isGoodDay=true일 때만 세팅)")
  private Integer goodDayCharge;

  @Schema(description = "주말 추가금 (isWeekend=true일 때만 세팅)")
  private Integer weekendCharge;

  private EstimateStatus status;

}
