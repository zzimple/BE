package com.zzimple.estimate.guest.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimateResponsePreview {
  private Long storeId;
  private String storeName;
  private Integer truckCount;
  private String ownerMessage;
  private LocalDateTime respondedAt;
  private int itemsTotal;
  private int extraTotal;
  private int finalTotal;
}
