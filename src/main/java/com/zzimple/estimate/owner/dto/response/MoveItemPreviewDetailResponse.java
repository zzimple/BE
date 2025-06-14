package com.zzimple.estimate.owner.dto.response;

import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.MoveItemCategory;
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
@Schema(description = "이사 짐 항목 상세 정보 응답")
public class MoveItemPreviewDetailResponse {

  @Schema(description = "짐 항목 고유 ID")
  private Long id;

  @Schema(description = "짐 종류 ID")
  private Long itemTypeId;

  private String itemTypeName;

  @Schema(description = "짐 카테고리", example = "FURNITURE")
  private MoveItemCategory category;

  @Schema(description = "수량")
  private int quantity;

  @Schema(description = "항목 구체 타입")
  private String type;

  @Schema(description = "너비(cm)")
  private String width;

  @Schema(description = "높이(cm)")
  private String height;

  @Schema(description = "깊이(cm)")
  private String depth;

  @Schema(description = "재질")
  private String material;

  @Schema(description = "크기")
  private String size;

  @Schema(description = "형태")
  private String shape;

  @Schema(description = "용량")
  private String capacity;

  @Schema(description = "문 개수")
  private Integer doorCount;

  @Schema(description = "단위 개수")
  private Integer unitCount;

  @Schema(description = "프레임 정보")
  private String frame;

  @Schema(description = "유리 포함 여부")
  private boolean hasGlass;

  @Schema(description = "접이식 여부")
  private boolean foldable;

  @Schema(description = "바퀴 포함 여부")
  private boolean hasWheels;

  @Schema(description = "프린터 포함 여부")
  private boolean hasPrinter;

  @Schema(description = "정수기 유형")
  private String purifierType;

  @Schema(description = "특이사항")
  private String specialNote;

  public static MoveItemPreviewDetailResponse from(MoveItems item) {
    return MoveItemPreviewDetailResponse.builder()
        .id(item.getId())
        .itemTypeId(item.getItemTypeId())
        .itemTypeName(item.getItemTypeName())
        .category(item.getCategory())
        .quantity(item.getQuantity())
        .type(item.getType())
        .width(item.getWidth())
        .height(item.getHeight())
        .depth(item.getDepth())
        .material(item.getMaterial())
        .size(item.getSize())
        .shape(item.getShape())
        .capacity(item.getCapacity())
        .doorCount(item.getDoorCount())
        .unitCount(item.getUnitCount())
        .frame(item.getFrame())
        .hasGlass(item.isHasGlass())
        .foldable(item.isFoldable())
        .hasWheels(item.isHasWheels())
        .hasPrinter(item.isHasPrinter())
        .purifierType(item.getPurifierType())
        .specialNote(item.getSpecialNote())
        .build();
  }
}
