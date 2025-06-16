package com.zzimple.estimate.guest.entity;

import com.zzimple.estimate.guest.enums.MoveItemCategory;
import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "moveItems",
    indexes = {
        @Index(name = "idx_estimate_no", columnList = "estimate_no"),
        @Index(name = "idx_store_id", columnList = "store_id")
    }
)
public class MoveItems extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "estimate_no", nullable = false)
  private Long estimateNo;

  @Column(name = "store_id")
  private Long storeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MoveItemCategory category;

  @Column(nullable = false)
  private Long itemTypeId;

  @Column(nullable = false)
  private String itemTypeName;

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = true, columnDefinition = "INT DEFAULT 0")
  private Integer basePrice = 0;

  private String type;
  private String width;
  private String height;
  private String depth;
  private String material;
  private String size;
  private String shape;
  private String capacity;

  private Integer doorCount;
  private Integer unitCount;
  private String frame;

  private boolean hasGlass;
  private boolean foldable;
  private boolean hasWheels;
  private boolean hasPrinter;

  private String purifierType;

  private String specialNote;

  private Integer per_item_totalPrice;
}
