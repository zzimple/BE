package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.EstimateExtraCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateExtraChargeRepository extends JpaRepository<EstimateExtraCharge, Long> {

  List<EstimateExtraCharge> findByEstimateNo(Long estimateNo);

  void deleteByEstimateNo(Long estimateNo);
}
