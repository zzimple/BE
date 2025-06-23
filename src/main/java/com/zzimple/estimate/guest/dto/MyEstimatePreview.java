package com.zzimple.estimate.guest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyEstimatePreview {

  private Long estimateNo;
  private String moveDate;
  private String fromAddr;
  private String toAddr;
  private LocalDateTime createdAt;
  private int responseCount; // 사장님 응답 수
}
