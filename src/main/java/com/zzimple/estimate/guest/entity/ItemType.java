package com.zzimple.estimate.guest.entity;

import com.zzimple.estimate.guest.enums.MoveItemCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "item_type")
public class ItemType {

  @Id
  private Long itemTypeId;

  @Column(nullable = false)
  private String itemTypeName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MoveItemCategory category;  // FURNITURE, APPLIANCE, OTHER
}
