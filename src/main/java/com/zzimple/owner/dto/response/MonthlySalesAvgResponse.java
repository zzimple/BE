package com.zzimple.owner.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlySalesAvgResponse {
  private String month;      // "2025-06"
  private Double average;
  private List<MonthlySalesItemResponse> items;// 평균 매출
}