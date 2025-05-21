package com.zzimple.estimate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "reservation_item")
public class MoveItems {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 견적(Estimate)과 연결
  @Column(nullable = false)
  private Long estimateId;

  // 짐 종류
  @Column(nullable = false)
  private Long itemTypeId;

  @Column(nullable = false)
  private int quantity;

  private String width;
  private String height;
  private String material;

  @Column(columnDefinition = "TINYINT(1)", nullable = true)
  private boolean hasGlass;

  private String size;
  private String shape;
  private String capacity;

  @Column(columnDefinition = "json")
  private String details;  // JSON 문자열로 저장

  @Column(columnDefinition = "TEXT")
  private String requestNote;

  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}
