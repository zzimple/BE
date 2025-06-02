package com.zzimple.estimate.owner.dto.response;

import com.zzimple.estimate.guest.entity.Address;
import com.zzimple.estimate.guest.entity.AddressDetailInfo;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "견적서 상세 정보 응답")
public class EstimatePreviewDetailResponse {

  private Long estimateNo;
  private Long userId;
  private String moveDate;
  private MoveType moveType;
  private MoveOptionType optionType;

  private Address fromAddress;
  private AddressDetailInfo fromDetailInfo;

  private Address toAddress;
  private AddressDetailInfo toDetailInfo;

  private List<MoveItemPreviewDetailResponse> items;

  private String customerMemo;

  public static EstimatePreviewDetailResponse fromEntity(Estimate estimate, List<MoveItems> moveItems) {
    return EstimatePreviewDetailResponse.builder()
        .estimateNo(estimate.getEstimateNo())
        .userId(estimate.getUserId())
        .moveDate(estimate.getMoveDate())
        .moveType(estimate.getMoveType())
        .optionType(estimate.getOptionType())
        .fromAddress(estimate.getFromAddress())
        .fromDetailInfo(estimate.getFromDetail())
        .toAddress(estimate.getToAddress())
        .toDetailInfo(estimate.getToDetail())
        .customerMemo(estimate.getCustomerMemo())
        .items(moveItems.stream()
            .map(MoveItemPreviewDetailResponse::from) // 여기를 위해 아래 2번을 정의해야 함
            .toList())
        .build();
    }
}