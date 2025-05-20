package com.zzimple.estimate.entity;

import com.zzimple.estimate.enums.EstimateStatus;
import com.zzimple.estimate.enums.MoveType;
import com.zzimple.estimate.enums.PackingType;
import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "estimate")
public class Estimate extends BaseTimeEntity {

  // 아직 수정 중...

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long customerId;
  private Long ownerId;

  private MoveType moveType;  // 소형이사 or 가정이사
  private PackingType packingType;  // 일반이사 or 반포장 이사 or 포장 이사

  private LocalDateTime scheduledAt;  // 이사 예정 일시

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "roadFullAddr", column = @Column(name = "from_road_full_addr")),
      @AttributeOverride(name = "roadAddr", column = @Column(name = "from_road_addr")),
      @AttributeOverride(name = "zipNo", column = @Column(name = "from_zip_no")),
      @AttributeOverride(name = "addrDetail", column = @Column(name = "from_addr_detail")),
      @AttributeOverride(name = "buldMgtNo1", column = @Column(name = "from_buld_mgt_no1")),
      @AttributeOverride(name = "buldMgtNo2", column = @Column(name = "from_buld_mgt_no2"))
  })
  private Address fromAddress;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "roadFullAddr", column = @Column(name = "to_road_full_addr")),
      @AttributeOverride(name = "roadAddr", column = @Column(name = "to_road_addr")),
      @AttributeOverride(name = "zipNo", column = @Column(name = "to_zip_no")),
      @AttributeOverride(name = "addrDetail", column = @Column(name = "to_addr_detail")),
      @AttributeOverride(name = "buldMgtNo1", column = @Column(name = "to_buld_mgt_no1")),
      @AttributeOverride(name = "buldMgtNo2", column = @Column(name = "to_buld_mgt_no2"))
  })
  private Address toAddress;

  private String moveDate;

  private Integer fromFloor;
  private Integer toFloor;
  private Boolean hasElevator;

  @Enumerated(EnumType.STRING)
  private EstimateStatus status;

  private String customerMemo;

  // 사장님이 입력하는 항목
  private Integer truckTon;
  private Integer truckCount;
  private String ownerMemo;
}
