package com.zzimple.estimate.guest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "견적서 최종 불러오기 응답")
public class EstimateDraftFullResponse {
  private AddressFullResponse address;
  private HolidaySaveResponse holiday;
  private MoveTypeResponse moveType;
  private MoveItemsDraftResponse moveItems;
  private MoveOptionTypeResponse moveOption;
}
