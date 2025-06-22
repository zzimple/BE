package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.EstimateResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateResponseRepository extends JpaRepository<EstimateResponse, Long> {
  List<EstimateResponse> findAllByStoreId(Long storeId);
  List<EstimateResponse> findByEstimateNo(Long estimateNo);
}

