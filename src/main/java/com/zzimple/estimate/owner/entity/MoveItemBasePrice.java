package com.zzimple.estimate.owner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "item_base_price",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_owner_item_type", columnNames = {"owner_id", "item_type_id"})
    }
)
public class MoveItemBasePrice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "owner_id", nullable = false)
  private Long storeId;

  @Column(name = "item_type_id", nullable = false)
  private Long itemTypeId;

  @Column(name = "base_price", nullable = false)
  private int basePrice;

  public void updatePrice(int newPrice) {
    this.basePrice = newPrice;
  }
}
