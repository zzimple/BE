package com.zzimple.owner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WeeklySalesSimpleResponse {
  private String weekStartDate; // 매주 월요일 기준
  private int totalAmount;
}
