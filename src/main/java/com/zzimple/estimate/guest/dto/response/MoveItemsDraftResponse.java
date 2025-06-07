package com.zzimple.estimate.guest.dto.response;

import com.zzimple.estimate.guest.enums.MoveItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "단일 짐 항목 응답")
public class MoveItemsDraftResponse {

  @Schema(description = "전체 박스 개수")
  private Integer boxCount;

  @Schema(description = "잔짐 박스 개수")
  private int leftoverBoxCount;

  @Schema(description = "고객 요청사항 메모")
  private String requestNote;

  @Schema(description = "현재 저장된 짐 항목 리스트")
  private List<MoveItemResponseDto> items;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "짐 항목 상세")
  public static class MoveItemResponseDto {
//    @Schema(description = "엔트리 고유 ID", example = "uuid")
//    private String entryId;

    @Schema(description = "짐 종류 ID", example = "101")
    private Long itemTypeId;

    @Schema(description = "짐 종류 이름", example = "냉장고")
    private String itemTypeName;

    @Schema(description = "짐 카테고리 (가전/가구/기타)", example = "APPLIANCE")
    private MoveItemCategory category;

    @Schema(description = "수량", example = "2")
    private int quantity;

    @Schema(description = "짐 종류 타입", example = "쇼파 종류 등")
    private String type;

    @Schema(description = "가로 길이(cm)", example = "120")
    private String width;

    @Schema(description = "세로 길이(cm)", example = "200")
    private String height;

    @Schema(description = "너비(cm)", example = "60")
    private String depth;

    @Schema(description = "재질", example = "원목")
    private String material;

    @Schema(description = "사이즈 분류 (소형/중형/대형)", example = "중형")
    private String size;

    @Schema(description = "형태 분류 (직사각형, 원형 등)", example = "직사각형")
    private String shape;

    @Schema(description = "용량 또는 무게", example = "10kg")
    private String capacity;

    @Schema(description = "문 개수", example = "3")
    private Integer doorCount;

    @Schema(description = "단위 개수", example = "2")
    private Integer unitCount;

    @Schema(description = "프레임 정보", example = "헤드+매트리스 일체형")
    private String frame;

    @Schema(description = "유리 포함 여부")
    private boolean glass;

    @Schema(description = "접이식 여부")
    private boolean foldable;

    @Schema(description = "바퀴 포함 여부")
    private boolean wheels;

    @Schema(description = "프린터 포함 여부")
    private boolean printer;

    @Schema(description = "정수기 유형", example = "냉온정")
    private String purifierType;

    @Schema(description = "요청사항", example = "분리 필요")
    private String specialNote;
  }
}
