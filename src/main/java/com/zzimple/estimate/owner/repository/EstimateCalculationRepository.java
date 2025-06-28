package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.EstimateCalculation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateCalculationRepository extends JpaRepository<EstimateCalculation, Long> {
  Optional<EstimateCalculation> findByEstimateNo(Long estimateNo);
  List<EstimateCalculation> findByStoreId(Long storeId); // ✅ 이거 추가!
  Optional<EstimateCalculation> findByEstimateNoAndStoreId(Long estimateNo, Long storeId);
}
