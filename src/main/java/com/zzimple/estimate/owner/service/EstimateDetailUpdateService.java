package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.EstimateStatus;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SubmitFinalEstimateRequest;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest.ExtraChargeRequest;
import com.zzimple.estimate.owner.dto.response.CalculateOwnerInputResponse;
import com.zzimple.estimate.owner.entity.EstimateCalculation;
import com.zzimple.estimate.owner.entity.EstimateExtraCharge;
import com.zzimple.estimate.owner.entity.StorePriceSetting;
import com.zzimple.estimate.owner.exception.EstimateErrorCode;
import com.zzimple.estimate.owner.repository.EstimateCalculationRepository;
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
  private final EstimateCalculationRepository estimateCalculationRepository;

  @Transactional
  public void saveOwnerInput(Long storeId, Long estimateNo, SubmitFinalEstimateRequest request) {

    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적서를 찾을 수 없습니다. id=" + estimateNo));

    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new EntityNotFoundException("Store not found with id=" + storeId));

    Integer truckCount = request.getTruckCount();

    if (truckCount != null) {
      estimate.setTruckCount(truckCount);
    }

    List<ExtraChargeRequest> extraCharges = request.getExtraCharges();
    if (extraCharges != null) {
      for (ExtraChargeRequest ec : extraCharges) {
        saveExtraCharge(estimateNo, storeId, ec.getReason(), ec.getAmount());
      }
    }

    // 2) 단가 설정 조회
    StorePriceSetting setting = storePriceSettingRepository.findById(storeId)
        .orElseThrow(() -> new CustomException(
            EstimateErrorCode.STORE_PRICE_SETTING_NOT_FOUND));

    // 4) 공휴일
    if (Boolean.TRUE.equals(estimate.getIsHoliday())) {
      saveExtraCharge(estimateNo, storeId,
          "공휴일 추가금",
          setting.getHolidayCharge());
    }

    // 5) 손 없는 날
    if (Boolean.TRUE.equals(estimate.getIsGoodDay())) {
      saveExtraCharge(estimateNo, storeId,
          "손 없는 날 추가금",
          setting.getGoodDayCharge());
    }

    // 6) 주말
    if (Boolean.TRUE.equals(estimate.getIsWeekend())) {
      saveExtraCharge(estimateNo, storeId,
          "주말 추가금",
          setting.getWeekendCharge());
    }

    estimate.setOwnerMessage(request.getOwnerMessage());
    estimate.setStoreId(storeId);
    estimate.setStoreName(store.getName());
    estimate.setStatus(EstimateStatus.ACCEPTED);
    estimate.setTruckCount(request.getTruckCount());
    estimateRepository.save(estimate);

    log.info("[EstimateDetailUpdateService] 사장님 입력 저장 완료 - estimateNo: {} message: {}",
        estimateNo, request.getOwnerMessage());
  }

  @Transactional
  public CalculateOwnerInputResponse calculateAndSaveFinalTotals(Long storeId, Long estimateNo) {
    // 1) EstimateCalculation 조회 또는 새로 생성
    EstimateCalculation estimateCalculation = estimateCalculationRepository.findByEstimateNo(
            estimateNo)
        .orElseGet(() -> {
          EstimateCalculation e = new EstimateCalculation();
          e.setEstimateNo(estimateNo);
          e.setStoreId(storeId);
          return e;
        });

    // 2) DB에서 items_total_price 꺼내기
    int itemsTotal = estimateCalculation.getItemsTotalPrice();

    // 3) DB에서 추가금 합산
    int extraTotal = estimateExtraChargeRepository.findByEstimateNo(estimateNo).stream()
        .mapToInt(EstimateExtraCharge::getAmount)
        .sum();

    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new IllegalArgumentException("해당 견적이 없습니다."));

    int truckCount = estimate.getTruckCount();

    StorePriceSetting setting = storePriceSettingRepository.findByStoreId(storeId)
        .orElseThrow(() -> new IllegalArgumentException("사장님 가격 설정 정보가 없습니다."));

    int perTruckCharge = setting.getPerTruckCharge();

    int truckCountCharge = truckCount * perTruckCharge;

    // 4) 계산된 값 반영
    estimateCalculation.setExtraChargesTotal(extraTotal);
    estimateCalculation.setTruck_charge(truckCountCharge);
    estimateCalculation.setFinalTotalPrice(itemsTotal + extraTotal + truckCountCharge);

    // 5) 저장
    estimateCalculationRepository.save(estimateCalculation);

    log.info("[calculateAndSaveFinalTotals] estNo={}, itemsTotal={}, extraTotal={}, finalTotal={}",
        estimateNo, itemsTotal, extraTotal, itemsTotal + extraTotal);

    // 6) DTO로 반환
    return CalculateOwnerInputResponse.builder()
        .itemTotal(itemsTotal)
        .extraTotal(extraTotal)
        .truck_charge(truckCountCharge)
        .finalTotal(itemsTotal + extraTotal + truckCountCharge)
        .build();
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
