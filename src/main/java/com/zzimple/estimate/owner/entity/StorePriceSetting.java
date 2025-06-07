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
@Table(name = "store_price_setting")
public class StorePriceSetting extends BaseTimeEntity {
  @Id
  private Long storeId;

  private Integer perTruckCharge;
  private Integer holidayCharge;
  private Integer goodDayCharge;
  private Integer weekendCharge;
}
