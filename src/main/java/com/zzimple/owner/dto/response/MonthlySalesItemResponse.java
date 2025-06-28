package com.zzimple.owner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlySalesItemResponse {
  private Long id;
  private String date;
  private String customer;
  private Integer amount;
  private String status;
}
