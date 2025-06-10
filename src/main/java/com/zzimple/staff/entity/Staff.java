package com.zzimple.staff.entity;

import com.zzimple.global.common.BaseTimeEntity;
import com.zzimple.staff.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "staffs", indexes = {
    @Index(name = "idx_staff_user_id", columnList = "user_id"),
    @Index(name = "idx_staff_owner_id", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Staff extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "staff_id", nullable = false, unique = true)
  private Long staffId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @Column(name = "store_id", nullable = false)
  private Long storeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;
}
