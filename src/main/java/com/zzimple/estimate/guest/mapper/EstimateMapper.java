package com.zzimple.estimate.guest.mapper;

import com.zzimple.estimate.guest.dto.request.AddressWithDetailRequest;
import com.zzimple.estimate.guest.dto.response.EstimateDraftFullResponse;
import com.zzimple.estimate.guest.dto.response.HolidaySaveResponse;
import com.zzimple.estimate.guest.dto.response.MoveItemsDraftResponse;
import com.zzimple.estimate.guest.entity.Address;
import com.zzimple.estimate.guest.entity.AddressDetailInfo;
import com.zzimple.estimate.guest.entity.Estimate;

public class EstimateMapper {

  public static Estimate toEntity(EstimateDraftFullResponse draft, Long userId) {

    AddressWithDetailRequest from = draft.getAddress().getFromAddress();
    AddressWithDetailRequest to = draft.getAddress().getToAddress();

    AddressDetailInfo fromDetail = from.getDetailInfo().toEntity();
    AddressDetailInfo toDetail = from.getDetailInfo().toEntity();

    Address fromAddress = from.getAddress().toEntity();
    Address toAddress = to.getAddress().toEntity();

    HolidaySaveResponse holiday = draft.getHoliday();

    MoveItemsDraftResponse moveItems = draft.getMoveItems();

    return Estimate.of(
        userId,
        draft.getMoveType().getMoveType(),
        draft.getMoveOption().getOptionType(),
        draft.getHoliday().getScheduledAt(),
        fromAddress,
        toAddress,
        draft.getHoliday().getScheduledAt().toLocalDate().toString().replace("-", ""),
        draft.getMoveItems().getRequestNote(),
        fromDetail,
        toDetail,
        holiday.isGoodDay(),
        holiday.isHoliday(),
        holiday.isWeekend(),
        moveItems.getBoxCount(),
        moveItems.getLeftoverBoxCount()
    );
  }
}
