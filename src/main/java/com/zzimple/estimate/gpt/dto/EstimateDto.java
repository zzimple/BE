package com.zzimple.estimate.gpt.dto;

import com.zzimple.estimate.guest.entity.Address;
import com.zzimple.estimate.guest.entity.AddressDetailInfo;
import com.zzimple.estimate.guest.enums.MoveOptionType;
import com.zzimple.estimate.guest.enums.MoveType;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import com.zzimple.estimate.owner.dto.response.MoveItemPreviewDetailResponse;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

// 필요 필드만 뽑은 GPT 비교용 EstimateDto 생성
@Getter
@Setter
public class EstimateDto {

  private Integer truckCount;

  private Integer truckTotalPrice;

  private String ownerMessage;

  private List<SaveEstimatePriceRequest.ExtraChargeRequest> itemExtraCharges;

  private List<SaveEstimatePriceRequest.ExtraChargeRequest> extraCharges;

  private Integer totalPrice;
  private Integer holidayCharge;
  private Integer goodDayCharge;
  private Integer weekendCharge;
}
