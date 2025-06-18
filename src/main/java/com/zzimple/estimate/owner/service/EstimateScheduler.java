package com.zzimple.estimate.owner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstimateScheduler {

  private final EstimateStatusSchedulerService estimateStatusSchedulerService;

  @Scheduled(cron = "0 0 0 * * *") // 매일 자정
  public void runEstimateAutoCompletion() {
    estimateStatusSchedulerService.autoCompletePastEstimates();
  }
}
