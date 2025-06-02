package com.zzimple.estimate.owner.dto.response;

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
@Schema(description = "견적 항목별 금액 목록과 총 금액을 포함하는 응답 DTO")
public class ItemTotalResultResponse {
  private int totalPrice;
  private List<ItemTotalResponse> items;
}
