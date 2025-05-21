package com.zzimple.estimate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "단일 짐 항목 응답")
public class MoveItemsDraftResponse {

  @Schema(description = "현재 저장된 짐 항목 리스트")
  private List<MoveItemResponseDto> items;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor

  public static class MoveItemResponseDto {
    @Schema(description = "엔트리 고유 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private String entryId;

    @Schema(description = "짐 종류 ID", example = "101")
    private Long itemTypeId;

    @Schema(description = "수량", example = "2")
    private int quantity;

    @Schema(description = "가로 길이(cm)", example = "120", nullable = true)
    private String width;

    @Schema(description = "세로 길이(cm)", example = "200", nullable = true)
    private String height;

    @Schema(description = "너비(cm)", example = "60", nullable = true)
    private String depth;

    @Schema(description = "재질", example = "원목", nullable = true)
    private String material;

    @Schema(description = "사이즈 분류 (소형/중형/대형)", example = "중형", nullable = true)
    private String size;

    @Schema(description = "형태 분류 (직사각형, 원형 등)", example = "직사각형", nullable = true)
    private String shape;

    @Schema(description = "용량 또는 무게", example = "10kg", nullable = true)
    private String capacity;

    @Schema(description = "문 개수", example = "3", nullable = true)
    private Integer doorCount;

    @Schema(description = "단위 개수 (예: 구성품 수량, 서랍 수 등)", example = "2", nullable = true)
    private Integer unitCount;

    @Schema(description = "프레임 정보", example = "헤드+매트리스 일체형", nullable = true)
    private String frame;

    @Schema(description = "유리 포함 여부", example = "false")
    private boolean hasGlass;

    @Schema(description = "접이식 여부", example = "false")
    private boolean foldable;

    @Schema(description = "바퀴 여부", example = "false")
    private boolean hasWheels;

    @Schema(description = "프린터 포함 여부", example = "false")
    private boolean hasPrinter;

    @Schema(description = "정수기 유형", example = "냉온정", nullable = true)
    private String purifierType;

    @Schema(description = "에어컨 유형", example = "스탠드형", nullable = true)
    private String acType;

    @Schema(description = "특이사항", example = "분리 필요", nullable = true)
    private String specialNote;

    @Schema(description = "고객 요청사항 메모", example = "엘리베이터 없음, 창문 진입 요청", nullable = true)
    private String requestNote;

    @Schema(description = "기타 옵션 정보 (키-값 쌍)", nullable = true)
    private Map<String, Object> details;

    @Schema(description = "짐 박스", example = "1")
    private Integer box;
  }
}
