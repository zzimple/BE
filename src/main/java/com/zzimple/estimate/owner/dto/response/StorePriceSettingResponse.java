package com.zzimple.estimate.owner.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorePriceSettingResponse {
  private int perTruckCharge;
  private int holidayCharge;
  private int goodDayCharge;
  private int weekendCharge;
}
