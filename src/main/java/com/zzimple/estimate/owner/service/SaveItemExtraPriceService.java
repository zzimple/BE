package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.ItemType;
import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.exception.MoveItemErrorCode;
import com.zzimple.estimate.guest.repository.ItemTypeRepository;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SaveEstimatePriceRequest;
import com.zzimple.estimate.owner.dto.response.ItemTotalResponse;
import com.zzimple.estimate.owner.dto.response.ItemTotalResultResponse;
import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import com.zzimple.estimate.owner.repository.MoveItemExtraChargeRepository;
import com.zzimple.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveItemExtraPriceService {

  private final MoveItemsRepository moveItemsRepository;
  private final MoveItemExtraChargeRepository moveItemExtraChargeRepository;
  private final ItemTypeRepository itemTypeRepository;

  @Transactional
  public void saveEstimateItems(Long estimateNo, List<SaveEstimatePriceRequest> itemRequests) {
    List<MoveItems> existingItems = moveItemsRepository.findByEstimateNo(estimateNo);

    for (SaveEstimatePriceRequest item : itemRequests) {
      ItemType itemType = itemTypeRepository.findById(item.getItemTypeId())
          .orElseThrow(() -> new CustomException(MoveItemErrorCode.INVALID_ITEM_TYPE_ID));

      // 중복 여부 판단
      boolean isDuplicate = existingItems.stream().anyMatch(existing -> {
        if (!existing.getItemTypeId().equals(item.getItemTypeId())) return false;
        if (existing.getQuantity() != item.getQuantity()) return false;
        if (existing.getBasePrice() != item.getBasePrice()) return false;

        List<MoveItemExtraCharge> existingExtras =
            moveItemExtraChargeRepository.findByMoveItemId(existing.getId());

        // 기존 옵션들과 새로운 옵션이 동일한지 비교
        if ((existingExtras == null || existingExtras.isEmpty())
            && (item.getExtraCharges() == null || item.getExtraCharges().isEmpty())) {
          return true;
        }

        if (existingExtras.size() != item.getExtraCharges().size()) return false;

        // 두 리스트가 동일한지 비교
        return existingExtras.stream().allMatch(e ->
            item.getExtraCharges().stream().anyMatch(i ->
                i.getAmount() == e.getAmount() && i.getReason().equals(e.getReason()))
        );
      });

      if (isDuplicate) {
        log.info("중복된 항목이므로 저장하지 않음: {}", item.getItemTypeId());
        continue;
      }

      // 중복이 아니면 저장
      MoveItems moveItem = MoveItems.builder()
          .estimateNo(estimateNo)
          .itemTypeId(item.getItemTypeId())
          .itemTypeName(itemType.getItemTypeName())
          .quantity(item.getQuantity())
          .basePrice(item.getBasePrice())
          .category(itemType.getCategory())
          .build();

      MoveItems savedItem = moveItemsRepository.save(moveItem);

      if (item.getExtraCharges() != null && !item.getExtraCharges().isEmpty()) {
        List<MoveItemExtraCharge> extraCharges = item.getExtraCharges().stream()
            .map(extra -> MoveItemExtraCharge.builder()
                .moveItemId(savedItem.getId())
                .estimateNo(estimateNo)
                .amount(extra.getAmount())
                .reason(extra.getReason())
                .build())
            .toList();

        moveItemExtraChargeRepository.saveAll(extraCharges);
      }
    }
  }


  @Transactional
  public ItemTotalResultResponse calculateAndSaveItemTotalPrices(Long estimateNo) {
    List<MoveItems> items = moveItemsRepository.findByEstimateNo(estimateNo);

    List<ItemTotalResponse> responses = new ArrayList<>();
    int totalPrice = 0;

    for (MoveItems item : items) {
      int baseTotal = calculateBaseTotal(item);
      int extraTotal = calculateExtraTotal(item.getId());
      int itemTotalPrice = baseTotal + extraTotal;

      item.setItem_totalPrice(itemTotalPrice);
      responses.add(toItemTotalResponse(item, baseTotal, extraTotal, itemTotalPrice));

      totalPrice += itemTotalPrice; // ✅ 전체 총합 누적
      log.info("견적 항목 ID {} → base: {}, extra: {}, total: {}", item.getId(), baseTotal, extraTotal, itemTotalPrice);
    }

    moveItemsRepository.saveAll(items);

    // ✅ 전체 금액과 아이템 리스트를 함께 반환
    return new ItemTotalResultResponse(totalPrice, responses);
  }

  private int calculateBaseTotal(MoveItems item) {
    return item.getBasePrice() * item.getQuantity();
  }

  private int calculateExtraTotal(Long itemId) {
    return moveItemExtraChargeRepository.findByMoveItemId(itemId)
        .stream()
        .mapToInt(MoveItemExtraCharge::getAmount)
        .sum();
  }

  private ItemTotalResponse toItemTotalResponse(MoveItems item, int baseTotal, int extraTotal, int totalPrice) {
    return new ItemTotalResponse(
        item.getId(),                 // itemId
        item.getItemTypeId(),        // itemTypeId
        item.getItemTypeName(),      // itemTypeName
        baseTotal,
        extraTotal,
        totalPrice
    );
  }
}
