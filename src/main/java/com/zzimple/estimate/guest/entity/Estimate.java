package com.zzimple.estimate.guest.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.Setter;


@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(
    name = "estimate",
    indexes = {
        @Index(name = "idx_estimate_user_id", columnList = "user_id"),
        @Index(name = "idx_estimate_store_id", columnList = "store_id")
    }
)
public class Estimate extends BaseTimeEntity {

  // 견적서 - 손님
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "estimate_no", nullable = false)
  private Long estimateNo;

  @Column(nullable = false)
  private Long userId;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "roadFullAddr", column = @Column(name = "from_road_full_addr")),
      @AttributeOverride(name = "roadAddrPart1", column = @Column(name = "from_road_addr_part1")),
      @AttributeOverride(name = "zipNo", column = @Column(name = "from_zip_no")),
      @AttributeOverride(name = "addrDetail", column = @Column(name = "from_addr_detail")),
      @AttributeOverride(name = "buldMgtNo", column = @Column(name = "from_buld_mgt_no")),
      @AttributeOverride(name = "buldSlno", column = @Column(name = "from_buld_slno")),
      @AttributeOverride(name = "emdNm", column = @Column(name = "from_emd_nm")),
      @AttributeOverride(name = "liNm", column = @Column(name = "from_li_nm")),
  })
  private Address fromAddress;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "roadFullAddr", column = @Column(name = "to_road_full_addr")),
      @AttributeOverride(name = "roadAddrPart1", column = @Column(name = "to_road_addr_part1")),
      @AttributeOverride(name = "zipNo", column = @Column(name = "to_zip_no")),
      @AttributeOverride(name = "addrDetail", column = @Column(name = "to_addr_detail")),
      @AttributeOverride(name = "buldMgtNo", column = @Column(name = "to_buld_mgt_no")),
      @AttributeOverride(name = "buldSlno", column = @Column(name = "to_buld_slno")),
      @AttributeOverride(name = "emdNm", column = @Column(name = "to_emd_nm")),
      @AttributeOverride(name = "liNm", column = @Column(name = "to_li_nm"))
  })
  private Address toAddress;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "floor", column = @Column(name = "from_floor")),
      @AttributeOverride(name = "hasStairs", column = @Column(name = "from_has_stairs")),
      @AttributeOverride(name = "hasParking", column = @Column(name = "from_has_parking")),
      @AttributeOverride(name = "elevator", column = @Column(name = "from_elevator")),
      @AttributeOverride(name = "buildingType", column = @Column(name = "from_building_type")),
      @AttributeOverride(name = "roomStructure", column = @Column(name = "from_room_structure")),
      @AttributeOverride(name = "sizeOption", column = @Column(name = "from_size_option"))
  })
  private AddressDetailInfo fromDetail;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "floor", column = @Column(name = "to_floor")),
      @AttributeOverride(name = "hasStairs", column = @Column(name = "to_has_stairs")),
      @AttributeOverride(name = "hasParking", column = @Column(name = "to_has_parking")),
      @AttributeOverride(name = "elevator", column = @Column(name = "to_elevator")),
      @AttributeOverride(name = "buildingType", column = @Column(name = "to_building_type")),
      @AttributeOverride(name = "roomStructure", column = @Column(name = "to_room_structure")),
      @AttributeOverride(name = "sizeOption", column = @Column(name = "to_size_option"))
  })
  private AddressDetailInfo toDetail;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MoveType moveType; // 소형이사 / 가정이사

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MoveOptionType optionType; // 일반 / 반포장 / 포장

  @Column(length = 8)
  private String moveDate;

  @Column(nullable = false)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // ISO 포맷
  private LocalDateTime moveTime;

  @Enumerated(EnumType.STRING)
  private EstimateStatus status;

  private String customerMemo;

  // 견적서 - 사장님
  private Long storeId;

  private String storeName;

  private Integer truckCount;

  @Column(length = 1000)
  private String ownerMessage;

  @Column(nullable = false)
  private Boolean isHoliday;

  @Column(nullable = false)
  private Boolean isGoodDay;

  @Column(nullable = false)
  private Boolean isWeekend;

  private Integer totalPrice;

  // 정적 팩토리 메서드
  public static Estimate of(
      Long userId,
      MoveType moveType,
      MoveOptionType optionType,
      LocalDateTime moveTime,
      Address fromAddress,
      Address toAddress,
      String moveDate,
      String customerMemo,
      AddressDetailInfo fromDetail,
      AddressDetailInfo toDetail,
      Boolean isGoodDay,
      Boolean isHoliday,
      Boolean isWeekend
  ) {
    Estimate estimate = new Estimate();
    estimate.userId = userId;
    estimate.moveType = moveType;
    estimate.optionType = optionType;
    estimate.moveTime = moveTime;
    estimate.fromAddress = fromAddress;
    estimate.toAddress = toAddress;
    estimate.moveDate = moveDate;
    estimate.customerMemo = customerMemo;
    estimate.fromDetail = fromDetail;
    estimate.toDetail = toDetail;
    estimate.isGoodDay = isGoodDay;
    estimate.isHoliday = isHoliday;
    estimate.isWeekend = isWeekend;
    return estimate;
  }
}
