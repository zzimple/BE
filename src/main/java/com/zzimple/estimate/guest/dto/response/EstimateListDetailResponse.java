package com.zzimple.estimate.guest.dto.response;


import com.zzimple.estimate.guest.entity.Address;
import com.zzimple.estimate.guest.entity.AddressDetailInfo;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import io.swagger.v3.oas.annotations.media.Schema;
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
  private MoveType moveType;
  private MoveOptionType optionType;

  private Address fromAddress;
  private AddressDetailInfo fromDetailInfo;

  private Address toAddress;
  private AddressDetailInfo toDetailInfo;

  private String customerMemo;

  @Schema(description = "트럭 개수", example = "2")
  private Integer truckCount;

  @Schema(description = "사장님이 고객에게 전달할 메시지", example = "이사 당일 안전하게 도와드리겠습니다!")
  private String ownerMessage;

  @Schema(description = "물품별 단가 및 추가금 리스트")
  private List<SaveEstimatePriceRequest> itemPriceDetails;

  @Schema(description = "기타 추가금 항목 리스트 (사유 + 금액)")
  private List<SaveEstimatePriceRequest.ExtraChargeRequest> extraCharges;

  private Integer totalPrice;

  @Getter
  @Setter
  public class AssignedStaffResponse {
    private Long staffId;
    private String staffName;
  }
}
