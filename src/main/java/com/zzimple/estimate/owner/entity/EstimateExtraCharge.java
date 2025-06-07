package com.zzimple.estimate.owner.entity;

import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "estimate_extra_charge",
    indexes = {
        @Index(name = "idx_estimate_no", columnList = "estimate_no")
    }
)
public class EstimateExtraCharge extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "estimate_no", nullable = false)
  private Long estimateNo;

  @Column(nullable = false)
  private Integer amount;

  @Column(length = 255, nullable = false)
  private String reason; // 예: "트럭 추가금 (2대)", "엘리베이터 없음", "공휴일 추가금"
}
