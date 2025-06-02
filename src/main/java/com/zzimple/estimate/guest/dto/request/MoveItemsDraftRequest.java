package com.zzimple.estimate.guest.dto.request;

import com.zzimple.estimate.guest.enums.MoveItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "단일 짐 항목 정보 요청")
public class MoveItemsDraftRequest {

  @Schema(description = "짐 종류 ID", example = "1001")
  private Long itemTypeId;

  @Schema(description = "짐 종류 이름", example = "냉장고")
  private String itemTypeName;

  @Schema(description = "엔트리 고유 ID (생성 시 null → 서버에서 UUID 발급)", example = "1")
  private String entryId;

  @Schema(description = "짐 카테고리 (가전/가구/기타)", example = "APPLIANCE")
  private MoveItemCategory category;

  @Schema(description = "수량", example = "2")
  private int quantity;

  @Schema(description = "종류", example = "쇼파 종류 등등..")
  private String type;

  // 크기 관련
  @Schema(description = "가로 길이(cm)", example = "120")
  private String width;

  @Schema(description = "세로 길이(cm)", example = "200")
  private String height;

  @Schema(description = "너비(cm)", example = "60")
  private String depth;

  // 재질/유형 관련
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

  @Schema(description = "단위 개수 (예: 구성품 수량, 서랍 수 등)", example = "2")
  private Integer unitCount;

  @Schema(description = "프레임 정보", example = "헤드+매트리스 일체형")
  private String frame;

  // 옵션 (boolean flag)
  @Schema(description = "유리 포함 여부", example = "true")
  private Boolean hasGlass;

  @Schema(description = "접이식 여부", example = "true")
  private Boolean isFoldable;

  @Schema(description = "바퀴 여부", example = "false")
  private Boolean hasWheels;

  @Schema(description = "프린터 포함 여부 (PC 관련)", example = "true")
  private Boolean hasPrinter;

  // 전자기기용 상세 분류
  @Schema(description = "정수기 유형", example = "냉온정")
  private String purifierType;

  // 고객 설명
  @Schema(description = "요청사항", example = "분리 필요")
  private String specialNote;
}
