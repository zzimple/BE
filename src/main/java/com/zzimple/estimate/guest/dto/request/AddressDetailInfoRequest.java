package com.zzimple.estimate.guest.dto.request;

import com.zzimple.estimate.guest.entity.AddressDetailInfo;
import com.zzimple.estimate.guest.enums.BuildingType;
import com.zzimple.estimate.guest.enums.RoomStructure;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "건물 상세 정보 요청")
public class AddressDetailInfoRequest {

  @Schema(description = "건물 종류", example = "APARTMENT")
  private BuildingType buildingType;

  @Schema(description = "방 구조", example = "THREE_ROOM")
  private RoomStructure roomStructure;

  @Schema(description = "평수", example = "10평 이하")
  private String sizeOption;

  @Schema(description = "층수", example = "3층")
  private String floor;

  @Schema(description = "1층 별도 계단 여부", example = "true")
  private boolean hasStairs;

  @Schema(description = "주차 가능 여부", example = "true")
  private boolean hasParking;

  @Schema(description = "엘리베이터 여부", example = "true")
  private boolean elevator;

  public AddressDetailInfo toEntity() {
    return new AddressDetailInfo(
        this.buildingType,
        this.roomStructure,
        this.sizeOption,
        parseFloor(this.floor),
        this.hasStairs,
        this.hasParking,
        this.elevator
    );
  }

  private Integer parseFloor(String floorText) {
    if (floorText == null)
      return null;
    try {
      return Integer.parseInt(floorText.replaceAll("[^0-9]", ""));
    } catch (NumberFormatException e) {
      return null;
    }
  }
}