package com.zzimple.estimate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "짐 항목 최종 저장 요청")
public class MoveItemsBatchRequest {

  @Schema(description = "모든 짐 항목 리스트")
  private List<MoveItemsDraftRequest> items;

  @Schema(description = "전체 박스 개수", example = "1")
  private Integer boxCount;

}
