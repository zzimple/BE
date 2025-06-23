package com.zzimple.estimate.guest.vision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "user_preferred_item",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_type_id"}),
    indexes = {
        @Index(name = "idx_user_preferred_user_id", columnList = "user_id"),
        @Index(name = "idx_user_preferred_item_type_id", columnList = "item_type_id")
    }
)
public class UserPreferredItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "item_type_id", nullable = false)
  private Long itemTypeId;  // INT PK로 관리하는 테이블이므로 Integer 사용


  public UserPreferredItem(Long userId, Long itemTypeId) {
    this.userId = userId;
    this.itemTypeId = itemTypeId;
  }
}
