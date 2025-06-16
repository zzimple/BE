package com.zzimple.estimate.owner.dto.response;

import com.zzimple.estimate.guest.entity.Address;
import com.zzimple.estimate.guest.entity.AddressDetailInfo;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "견적서 상세 정보 응답")
public class EstimatePreviewDetailResponse {

  private Long estimateNo;
  private Long userId;

  private String moveDate;
  private LocalDateTime moveTime;

  private MoveType moveType;
  private MoveOptionType optionType;

  private Address fromAddress;
  private AddressDetailInfo fromDetailInfo;

  private Address toAddress;
  private AddressDetailInfo toDetailInfo;

  private Integer boxCount;

  private int leftoverBoxCount;

  private boolean elevator;

  private List<MoveItemPreviewDetailResponse> items;

  private String customerMemo;


  private int furnitureCount;

  private int applianceCount;

  private int otherCount;

  @Schema(description = "트럭 개수", example = "1")
  private Integer truckCount;

  @Schema(description = "사장님 추가 말", example = "어쩌고 저쩌고")
  private String ownerMessage;

  public static EstimatePreviewDetailResponse fromEntity(Estimate estimate, List<MoveItems> moveItems, int furnitureCount, int applianceCount, int otherCount) {
    return EstimatePreviewDetailResponse.builder()
        .estimateNo(estimate.getEstimateNo())
        .userId(estimate.getUserId())
        .moveDate(estimate.getMoveDate())
        .moveTime(estimate.getMoveTime())
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
        .boxCount(estimate.getBoxCount())
        .leftoverBoxCount(estimate.getLeftoverBoxCount())
        .furnitureCount(furnitureCount)
        .applianceCount(applianceCount)
        .otherCount(otherCount)
        .truckCount(estimate.getTruckCount())
        .ownerMessage(estimate.getOwnerMessage())
        .build();
    }
}