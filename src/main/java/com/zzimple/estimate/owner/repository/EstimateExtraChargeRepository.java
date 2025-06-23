package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.EstimateExtraCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface EstimateExtraChargeRepository extends JpaRepository<EstimateExtraCharge, Long> {

  List<EstimateExtraCharge> findByEstimateNo(Long estimateNo);

  List<EstimateExtraCharge> findByEstimateNoAndStoreId(Long estimateNo, Long storeId);

  @Modifying
  @Transactional
  void deleteByEstimateNoAndStoreId(Long estimateNo, Long storeId);
}
