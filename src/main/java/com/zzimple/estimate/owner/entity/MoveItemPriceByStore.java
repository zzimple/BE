package com.zzimple.estimate.owner.entity;

import com.zzimple.estimate.guest.enums.MoveItemCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class MoveItemPriceByStore {

  @Id
  @GeneratedValue
  private Long id;

  private Long estimateNo;
  private Long storeId;
  private Long itemTypeId;
  private String storeName;
  private MoveItemCategory category;

  private Integer basePrice;
}

