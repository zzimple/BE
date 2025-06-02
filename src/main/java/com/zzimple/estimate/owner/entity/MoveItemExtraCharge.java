package com.zzimple.estimate.owner.entity;

import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "move_item_extra_charge",
    indexes = {
        @Index(name = "idx_move_item_id", columnList = "move_item_id")
    }
)
public class MoveItemExtraCharge extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "estimate_No", nullable = false)
  private Long estimateNo ;

  @Column(name = "move_item_id", nullable = false)
  private Long moveItemId; // MoveItems의 PK 값

  @Column(nullable = false)
  private int amount; // 추가 금액 (ex. 10000)

  @Column(length = 255)
  private String reason; // 추가금 사유 (ex. "계단 이동")

}
