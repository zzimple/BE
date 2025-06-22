package com.zzimple.estimate.owner.entity;

import com.zzimple.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "estimate_calculation",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_est_calc_est_no_store_id",
        columnNames = {"estimate_no", "store_id"}
    )
)
public class EstimateCalculation extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long estimateNo;

  private Long storeId;

  private int truck_charge;

  private int itemsTotalPrice;

  private int extraChargesTotal;

  private int finalTotalPrice;
}
