package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SubmitFinalEstimateRequest;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest.ExtraChargeRequest;
import com.zzimple.estimate.owner.entity.EstimateExtraCharge;
import com.zzimple.estimate.owner.entity.StorePriceSetting;
import com.zzimple.estimate.owner.exception.EstimateErrorCode;
import com.zzimple.estimate.owner.repository.EstimateExtraChargeRepository;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.estimate.owner.repository.StorePriceSettingRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstimateDetailUpdateService {

  private final EstimateRepository estimateRepository;
  private final StorePriceSettingRepository storePriceSettingRepository;
  private final EstimateExtraChargeRepository estimateExtraChargeRepository;
  private final MoveItemsRepository moveItemsRepository;
  private final StoreRepository storeRepository;

  @Transactional
  public void saveOwnerInput(Long storeId, Long estimateNo, SubmitFinalEstimateRequest request) {

    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적서를 찾을 수 없습니다. id=" + estimateNo));

    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new EntityNotFoundException("Store not found with id=" + storeId));

    StorePriceSetting setting = storePriceSettingRepository.findById(storeId)
        .orElseThrow(() -> new CustomException(EstimateErrorCode.STORE_PRICE_SETTING_NOT_FOUND));

    int totalExtraCharge = 0;

    // 1. 트럭 개수 추가금
    Integer truckCount = request.getTruckCount();
    if (truckCount != null && truckCount > 1) {
      int truckExtra = (truckCount - 1) * setting.getPerTruckCharge();
      totalExtraCharge += truckExtra;

      estimate.setTruckCount(truckCount);

      saveExtraCharge(estimateNo, storeId, "트럭 추가금 (" + truckCount + "대)", truckExtra);
    }

    // 2. 직접 입력한 추가 사유
    List<ExtraChargeRequest> extraCharges = request.getExtraCharges();
    if (extraCharges != null) {
      for (ExtraChargeRequest charge : extraCharges) {
        totalExtraCharge += charge.getAmount();
        saveExtraCharge(estimateNo,storeId, charge.getReason(), charge.getAmount());
      }
    }

    // 3. 날짜 조건에 따른 추가금
    if (estimate.getIsHoliday()) {
      totalExtraCharge += setting.getHolidayCharge();
      saveExtraCharge(estimateNo, storeId,"공휴일 추가금", setting.getHolidayCharge());
    }

    if (estimate.getIsGoodDay()) {
      totalExtraCharge += setting.getGoodDayCharge();
      saveExtraCharge(estimateNo, storeId,"손 없는 날 추가금", setting.getGoodDayCharge());
    }

    if (estimate.getIsWeekend()) {
      totalExtraCharge += setting.getWeekendCharge();
      saveExtraCharge(estimateNo, storeId,"주말 추가금", setting.getWeekendCharge());
    }

    //   1. moveItems 직접 조회 (기존 getMoveItems() 대신)
    List<MoveItems> moveItems = moveItemsRepository.findByEstimateNo(estimateNo);

    // 2. 각 item의 item_totalPrice 합산
    int itemTotalPrice = moveItems.stream()
        .mapToInt(item -> item.getItem_totalPrice() != null ? item.getItem_totalPrice() : 0)
        .sum();

    // 최종 견적 총금액 저장
    estimate.setTotalPrice(itemTotalPrice + totalExtraCharge);

    estimate.setOwnerMessage(request.getOwnerMessage());
    estimate.setStoreId(storeId);
    estimate.setStoreName(store.getName());
    estimate.setStatus(EstimateStatus.ACCEPTED);
    estimateRepository.save(estimate);

    log.info("[EstimateDetailUpdateService] 사장님 입력 저장 완료 - estimateNo: {}, totalExtraCharge: {}, message: {}",
        estimateNo, totalExtraCharge, request.getOwnerMessage());
  }

  private void saveExtraCharge(Long estimateNo, Long storeId, String reason, int amount) {
    EstimateExtraCharge charge = EstimateExtraCharge.builder()
        .estimateNo(estimateNo)
        .storeId(storeId)
        .reason(reason)
        .amount(amount)
        .build();
    estimateExtraChargeRepository.save(charge);
  }
}
