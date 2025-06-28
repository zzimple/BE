package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.owner.entity.EstimateOwnerResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateOwnerResponseRepository extends JpaRepository<EstimateOwnerResponse, Long> {
  Optional<EstimateOwnerResponse> findByEstimateNoAndStoreId(Long estimateNo, Long storeId);
  List<EstimateOwnerResponse> findByEstimateNo(Long estimateNo);
  int countByEstimateNo(Long estimateNo);
  Optional<EstimateOwnerResponse>
  findByEstimateNoAndStatus(Long estimateNo, EstimateStatus status);
  List<EstimateOwnerResponse> findByStatus(EstimateStatus status);
  List<EstimateOwnerResponse> findByEstimateNoInAndStatus(List<Long> estimateNos, EstimateStatus status);
}
