package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstimateDetailUpdateService {

  private final EstimateRepository estimateRepository;

  @Transactional
  public void saveOwnerInput(Long estimateNo, Long ownerId, Integer truckCount, String ownerMessage) {
    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적서를 찾을 수 없습니다. id=" + estimateNo));

    estimate.setTruckCount(truckCount);
    estimate.setOwnerMessage(ownerMessage);

    estimateRepository.save(estimate);

    log.info("[OwnerEstimateInputService] 사장님 입력 저장 완료 - estimateNo: {}, truckCount: {}, message: {}",
        estimateNo, truckCount, ownerMessage);
  }
}
