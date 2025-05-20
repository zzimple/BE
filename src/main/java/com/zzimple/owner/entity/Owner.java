package com.zzimple.owner.entity;

import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "owners", indexes = @Index(name = "idx_owner_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Owner extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, unique = true)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "business_number", nullable = false, unique = true)
  private String businessNumber;

  @Column(name = "insured")
//  @Column(name = "insured", nullable = false)
  private Boolean insured;

  @Column(name = "status")
  private String status;
}