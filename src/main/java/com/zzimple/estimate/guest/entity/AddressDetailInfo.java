package com.zzimple.estimate.guest.entity;

import com.zzimple.estimate.guest.enums.BuildingType;
import com.zzimple.estimate.guest.enums.RoomStructure;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@Schema(description = "주소 상세 정보")
public class AddressDetailInfo {

  @Schema(description = "건물 유형 (예: APARTMENT, VILLA, OFFICE)")
  private BuildingType buildingType;

  @Schema(description = "방 구조 (예: ONE_ROOM, THREE_ROOM 등)")
  private RoomStructure roomStructure;

  @Schema(description = "집 크기 옵션 (예: 10평 이하, 20평대 등)")
  private String sizeOption;

  @Schema(description = "층수")
  private Integer floor;

  @Schema(description = "계단 여부")
  private boolean hasStairs;

  @Schema(description = "주차 가능 여부")
  private boolean hasParking;

  @Schema(description = "엘리베이터 여부")
  private boolean elevator;

  public AddressDetailInfo(
      BuildingType buildingType,
      RoomStructure roomStructure,
      String sizeOption,
      Integer floor,
      boolean hasStairs,
      boolean hasParking,
      boolean elevator
  ) {
    this.buildingType = buildingType;
    this.roomStructure = roomStructure;
    this.sizeOption = sizeOption;
    this.floor = floor;
    this.hasStairs = hasStairs;
    this.hasParking = hasParking;
    this.elevator = elevator;
  }
}
