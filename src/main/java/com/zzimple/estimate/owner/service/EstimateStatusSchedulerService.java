package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EstimateStatusSchedulerService {

  private final EstimateRepository estimateRepository;

  @Transactional
  public void autoCompletePastEstimates() {
    LocalDate today = LocalDate.now();

    List<Estimate> confirmedEstimates = estimateRepository
        .findByStatus(EstimateStatus.CONFIRMED); // moveDate 필터는 Java에서 처리

    int completedCount = 0;

    for (Estimate estimate : confirmedEstimates) {
      try {
        // moveDate가 문자열이라면 LocalDate로 파싱
        LocalDate moveDate = LocalDate.parse(estimate.getMoveDate()); // "yyyy-MM-dd" 형식이어야 함

        if (moveDate.isBefore(today)) {
          estimate.setStatus(EstimateStatus.COMPLETED);
          completedCount++;
        }
      } catch (Exception e) {
        log.warn("[스케줄러] 잘못된 날짜 형식: estimateNo={}, moveDate={}", estimate.getEstimateNo(), estimate.getMoveDate());
      }
    }

    estimateRepository.saveAll(confirmedEstimates);
    log.info("[스케줄러] COMPLETED 처리된 견적서 수: {}", completedCount);
  }
}
