package com.zzimple.estimate.guest.enums;

public enum EstimateStatus {
  WAITING,  // 요청 상태
  ACCEPTED,   // 사장님 수락 (RESPONDED)
  CONFIRMED, // 손님이 최종 견적서 수락
  COMPLETED,   // 이사 완료됨 (자동 처리)
  REJECTED    // 거절
}
