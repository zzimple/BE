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
        @Index(name = "idx_item_type_id", columnList = "item_type_id"),
        @Index(name = "idx_move_item_store_id", columnList = "store_id")
    }
)
public class MoveItemExtraCharge extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "estimate_No", nullable = false)
  private Long estimateNo;

  @Column(name = "store_id", nullable = false)
  private Long storeId;

  @Column(name = "item_type_id", nullable = false)
  private Long itemTypeId;

  @Column(nullable = false)
  private int amount;

  @Column(length = 255)
  private String reason;

}
