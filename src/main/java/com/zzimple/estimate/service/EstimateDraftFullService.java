package com.zzimple.estimate.service;

import com.zzimple.estimate.dto.response.EstimateDraftFullResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstimateDraftFullService {

  private final AddressService addressService;
  private final HolidayService holidayService;
  private final MoveTypeService moveService;
  private final MoveItemsService moveItemsService;
  private final MoveOptionService smallMoveOptionService;

  public EstimateDraftFullResponse getFullDraft(UUID draftId) {
    return EstimateDraftFullResponse.builder()
        .address(addressService.getAddressDraft(draftId))
        .holiday(holidayService.getMoveDate(draftId))
        .moveType(moveService.getMoveType(draftId))
        .moveItems(moveItemsService.getMoveItemsAsResponse(draftId))
        .smallMoveOption(smallMoveOptionService.getOptionIfExist(draftId))
        .build();
  }
}
