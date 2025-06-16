package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import com.zzimple.estimate.owner.dto.response.ItemTotalResponse;
import com.zzimple.estimate.owner.dto.response.ItemTotalResultResponse;
import com.zzimple.estimate.owner.entity.EstimateCalculation;
import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import com.zzimple.estimate.owner.repository.EstimateCalculationRepository;
import com.zzimple.estimate.owner.repository.MoveItemExtraChargeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveItemExtraPriceService {

  private final MoveItemsRepository moveItemsRepository;
  private final MoveItemExtraChargeRepository moveItemExtraChargeRepository;
  private final EstimateCalculationRepository estimateCalculationRepository;

  @Transactional
  public void saveEstimateItems(Long estimateNo, Long storeId, List<SaveEstimatePriceRequest> itemRequests) {

    // moveitem의 기본금을 update하기.
    for (SaveEstimatePriceRequest req : itemRequests) {
      log.debug("기본금 업데이트: estimateNo={}, storeId={}, itemTypeId={}, newBasePrice={}",
        estimateNo, storeId, req.getItemTypeId(), req.getBasePrice());

      int updated = moveItemsRepository.updateBasePriceIfChanged(
          estimateNo,
          storeId,
          req.getItemTypeId(),
          req.getBasePrice()
      );
      log.debug("업데이트된 행 수: itemTypeId={}, updated={}", req.getItemTypeId(), updated);
    }

    // 2) item-level 추가금 저장: 추가 항목마다 직접 itemId 조회 후 처리
    for (SaveEstimatePriceRequest request : itemRequests) {
      log.debug("옵션 처리 시작: estimateNo={}, storeId={}, itemTypeId={}",
          estimateNo, storeId, request.getItemTypeId());
      // 2-1) estimateNo + storeId + itemTypeId 로 DB PK(itemId)만 조회 (findByEstimateNoAndItemTypeId 커스텀 메서드 필요)
      Long itemId = request.getItemTypeId();

      moveItemExtraChargeRepository.deleteByEstimateNoAndStoreIdAndItemTypeId(estimateNo, storeId, itemId);

      // 2-3) 새 옵션 리스트 insert
      if (request.getExtraCharges() != null) {
        List<MoveItemExtraCharge> toSave = request.getExtraCharges().stream()
            .map(ec -> MoveItemExtraCharge.builder()
                .estimateNo(estimateNo)
                .storeId(storeId)
                .itemTypeId(itemId)
                .amount(ec.getAmount())
                .reason(ec.getReason())
                .build())
            .collect(Collectors.toList());
        moveItemExtraChargeRepository.saveAll(toSave);
        log.debug("새 옵션 저장 완료: itemId={}, count={}", itemId, toSave.size());
      }
    }
    log.info("견적 저장 완료: estimateNo={}", estimateNo);
  }

  @Transactional
  public ItemTotalResultResponse calculateAndSaveItemTotalPrices(Long estimateNo, Long storeId) {

    List<MoveItems> items = moveItemsRepository.findByEstimateNo(estimateNo);

    List<ItemTotalResponse> responses = new ArrayList<>();
    int totalPrice = 0;

    for (MoveItems item : items) {
      int baseTotal = calculateBaseTotal(item);
      int extraTotal = calculateExtraTotal(
          item.getEstimateNo(),
          item.getStoreId(),
          item.getItemTypeId()
      );
      int itemTotalPrice = baseTotal + extraTotal;

      item.setPer_item_totalPrice(itemTotalPrice);
      responses.add(toItemTotalResponse(item, baseTotal, extraTotal, itemTotalPrice));

      totalPrice += itemTotalPrice;
      log.info("견적 항목 ID {} → base: {}, extra: {}, total: {}", item.getId(), baseTotal, extraTotal, itemTotalPrice);
    }

    moveItemsRepository.saveAll(items);

    EstimateCalculation estimateCalculation = estimateCalculationRepository.findByEstimateNo(estimateNo)
        .orElseGet(() -> EstimateCalculation.builder()
            .estimateNo(estimateNo)
            .storeId(storeId)
            .extraChargesTotal(0)
            .finalTotalPrice(0)
            .build()
        );

    estimateCalculation.setItemsTotalPrice(totalPrice);
    estimateCalculationRepository.save(estimateCalculation);

    // 전체 금액과 아이템 리스트를 함께 반환
    return new ItemTotalResultResponse(totalPrice, responses);
  }

  private int calculateBaseTotal(MoveItems item) {
    Integer base = item.getBasePrice();
    return base != null ? base : 0;
  }


  private int calculateExtraTotal(Long estimateNo, Long storeId, Long itemTypeId) {
    return moveItemExtraChargeRepository
        .findByEstimateNoAndStoreIdAndItemTypeId(estimateNo, storeId, itemTypeId)
        .stream()
        .mapToInt(MoveItemExtraCharge::getAmount)
        .sum();
  }

  private ItemTotalResponse toItemTotalResponse(MoveItems item, int baseTotal, int extraTotal, int totalPrice) {
    return new ItemTotalResponse(
        item.getId(),
        item.getItemTypeId(),
        item.getItemTypeName(),
        baseTotal,
        extraTotal,
        totalPrice
    );
  }
}
