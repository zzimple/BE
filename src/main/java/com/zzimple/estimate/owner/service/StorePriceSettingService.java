package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.owner.dto.request.SaveStorePriceSettingRequest;
import com.zzimple.estimate.owner.dto.response.StorePriceSettingResponse;
import com.zzimple.estimate.owner.entity.StorePriceSetting;
import com.zzimple.estimate.owner.repository.StorePriceSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StorePriceSettingService {

  private final StorePriceSettingRepository storePriceSettingRepository;

  @Transactional
  public void savePriceSetting(Long storeId, SaveStorePriceSettingRequest request) {
    StorePriceSetting setting = StorePriceSetting.builder()
        .storeId(storeId)
        .perTruckCharge(request.getPerTruckCharge())
        .holidayCharge(request.getHolidayCharge())
        .goodDayCharge(request.getGoodDayCharge())
        .weekendCharge(request.getWeekendCharge())
        .build();

    storePriceSettingRepository.save(setting);
  }

  @Transactional(readOnly = true)
  public StorePriceSettingResponse getPriceSetting(Long storeId) {
    return storePriceSettingRepository.findByStoreId(storeId)
        .map(setting -> StorePriceSettingResponse.builder()
            .perTruckCharge(setting.getPerTruckCharge())
            .holidayCharge(setting.getHolidayCharge())
            .goodDayCharge(setting.getGoodDayCharge())
            .weekendCharge(setting.getWeekendCharge())
            .build())
        .orElseGet(() -> StorePriceSettingResponse.builder()
            .perTruckCharge(0)
            .holidayCharge(0)
            .goodDayCharge(0)
            .weekendCharge(0)
            .build());
  }
}
