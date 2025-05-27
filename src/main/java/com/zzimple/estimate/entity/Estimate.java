package com.zzimple.estimate.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zzimple.estimate.enums.EstimateStatus;
import com.zzimple.estimate.enums.MoveOptionType;
import com.zzimple.estimate.enums.MoveType;
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
@Table(name = "estimate")
public class Estimate extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long estimate_No;

  @Column(nullable = false)
  private Long customerId;

  private Long ownerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MoveType moveType; // 소형이사 / 가정이사

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MoveOptionType optionType; // 일반 / 반포장 / 포장

  @Column(nullable = false)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // ISO 포맷
  private LocalDateTime scheduledAt;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "roadFullAddr", column = @Column(name = "from_road_full_addr")),
      @AttributeOverride(name = "roadAddr", column = @Column(name = "from_road_addr")),
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
      @AttributeOverride(name = "roadAddr", column = @Column(name = "to_road_addr")),
      @AttributeOverride(name = "zipNo", column = @Column(name = "to_zip_no")),
      @AttributeOverride(name = "addrDetail", column = @Column(name = "to_addr_detail")),
      @AttributeOverride(name = "buldMgtNo", column = @Column(name = "to_buld_mgt_no")),
      @AttributeOverride(name = "buldSlno", column = @Column(name = "to_buld_slno")),
      @AttributeOverride(name = "emdNm", column = @Column(name = "to_emd_nm")),
      @AttributeOverride(name = "liNm", column = @Column(name = "to_li_nm"))
  })
  private Address toAddress;

  @Column(length = 8)
  private String moveDate; // yyyyMMdd, 혹은 LocalDate 타입 권장

  private Integer fromFloor;

  private Integer toFloor;

  @Column(columnDefinition = "TINYINT(1)")
  private Boolean hasElevator;

  @Enumerated(EnumType.STRING)
  private EstimateStatus status;

  @Column(columnDefinition = "TEXT")
  private String customerMemo;

  // 정적 팩토리 메서드
  public static Estimate of(
      Long customerId,
      MoveType moveType,
      MoveOptionType optionType,
      LocalDateTime scheduledAt,
      Address fromAddress,
      Address toAddress,
      Integer fromFloor,
      Integer toFloor,
      Boolean hasElevator,
      String moveDate,
      String customerMemo
  ) {
    Estimate estimate = new Estimate();
    estimate.customerId = customerId;
    estimate.moveType = moveType;
    estimate.optionType = optionType;
    estimate.scheduledAt = scheduledAt;
    estimate.fromAddress = fromAddress;
    estimate.toAddress = toAddress;
    estimate.fromFloor = fromFloor;
    estimate.toFloor = toFloor;
    estimate.hasElevator = hasElevator;
    estimate.moveDate = moveDate;
    estimate.customerMemo = customerMemo;
    return estimate;
  }
}
