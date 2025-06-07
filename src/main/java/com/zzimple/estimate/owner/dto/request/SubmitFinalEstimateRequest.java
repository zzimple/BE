package com.zzimple.estimate.owner.dto.request;

import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest.ExtraChargeRequest;
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
@Schema(description = "사장님이 최종 제출하는 견적 확정 요청")
public class SubmitFinalEstimateRequest {

  @Schema(description = "트럭 개수", example = "2")
  private Integer truckCount;

  @Schema(description = "사장님이 고객에게 전달할 메시지", example = "이사 당일 안전하게 도와드리겠습니다!")
  private String ownerMessage;

  @Schema(description = "물품 및 단가 + 추가금 리스트")
  private List<SaveEstimatePriceRequest> items;

  @Schema(description = "기타 추가금 항목 리스트 (사유 + 금액)")
  private List<ExtraChargeRequest> extraCharges;

//  @Schema(description = "공휴일 여부", example = "true")
//  private boolean isHoliday;
//
//  @Schema(description = "손 없는 날 여부", example = "false")
//  private boolean isGoodDay;
//
//  @Schema(description = "주말 여부", example = "true")
//  private boolean isWeekend;
}
