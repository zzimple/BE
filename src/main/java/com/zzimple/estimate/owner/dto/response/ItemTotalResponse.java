package com.zzimple.estimate.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "각 이사 항목의 기본 금액, 추가금, 총 금액 정보를 담은 응답 DTO")
public class ItemTotalResponse {
  @Schema(description = "MoveItems 엔티티 ID")
  private Long itemId;

  @Schema(description = "아이템 종류 ID (item_type 테이블 참조)")
  private Long itemTypeId;

  @Schema(description = "아이템 이름 (item_type 테이블 참조)")
  private String itemTypeName;

  @Schema(description = "기본 금액 총합")
  private int baseTotal;

  @Schema(description = "추가 금액 총합")
  private int extraTotal;

  @Schema(description = "항목 총 금액 (기본 + 추가)")
  private int itemTotal;
}
