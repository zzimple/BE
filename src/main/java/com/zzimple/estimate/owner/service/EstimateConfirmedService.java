package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.owner.dto.response.EstimateConfirmedResponse;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstimateConfirmedService {

  private final EstimateRepository estimateRepository;

  // 아예 견적서 확정된 코드
  public Page<EstimateConfirmedResponse> getConfirmedEstimates(Long storeId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return estimateRepository.findByStoreIdAndStatus(storeId, EstimateStatus.CONFIRMED, pageable)
        .map(EstimateConfirmedResponse::from);
  }
}
