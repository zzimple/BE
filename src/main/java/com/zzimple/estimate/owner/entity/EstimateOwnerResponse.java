package com.zzimple.estimate.owner.entity;

import com.zzimple.estimate.guest.enums.EstimateStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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
public class EstimateOwnerResponse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long estimateNo;

  private Long storeId;

  private Integer truckCount;

  private String ownerMessage;

  private LocalDateTime respondedAt;

  @Enumerated(EnumType.STRING)
  private EstimateStatus status;  // WAITING, CONFIRMED, REJECTED 등
}
