package com.zzimple.estimate.owner.entity;

import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "store_price_setting",
    indexes = {
        @Index(name = "idx_store_price_store_id", columnList = "store_id")
    }
)
public class StorePriceSetting extends BaseTimeEntity {
  @Id
  private Long storeId;

  private Integer perTruckCharge;
  private Integer holidayCharge;
  private Integer goodDayCharge;
  private Integer weekendCharge;
}
