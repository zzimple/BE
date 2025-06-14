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
@Schema(description = "견적서 물품 리스트 응답")
public class EstimateMoveItemsListResponse {
  private List<MoveItemPreviewDetailResponse> items;
}
