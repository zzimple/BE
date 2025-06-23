package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.enums.MoveItemCategory;
import com.zzimple.estimate.guest.exception.MoveItemErrorCode;
import com.zzimple.estimate.guest.repository.ItemTypeRepository;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SaveItemBasePriceRequest;
import com.zzimple.estimate.owner.dto.response.EstimateItemWithExtraChargeResponse;
import com.zzimple.estimate.owner.dto.response.SaveItemBasePriceResponse;
import com.zzimple.estimate.owner.entity.MoveItemBasePrice;
import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import com.zzimple.estimate.owner.entity.MoveItemPriceByStore;
import com.zzimple.estimate.owner.repository.MoveItemBasePriceRepository;
import com.zzimple.estimate.owner.repository.MoveItemExtraChargeRepository;
import com.zzimple.estimate.owner.repository.MoveItemPriceByStoreRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.exception.StoreErrorCode;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.staff.exception.StaffErrorCode;
import com.zzimple.user.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaveItemBasePriceService {

  private final MoveItemBasePriceRepository basePriceRepository;
  private final ItemTypeRepository itemTypeRepository;
  private final OwnerRepository ownerRepository;
  private final StoreRepository storeRepository;
  private final MoveItemBasePriceRepository moveItemBasePriceRepository;
  private final MoveItemExtraChargeRepository moveItemExtraChargeRepository;
  private final MoveItemsRepository moveItemsRepository;
  private final MoveItemPriceByStoreRepository moveItemPriceByStoreRepository;

  @Transactional
  public List<SaveItemBasePriceResponse> saveOrUpdateAll(Long storeId,
      List<SaveItemBasePriceRequest> requests) {
    log.info("[BasePrice] 사장님 {}의 기본단가 {}개 저장 시도", storeId, requests.size());

    List<SaveItemBasePriceResponse> responses = requests.stream().map(request -> {
      Long itemTypeId = request.getItemTypeId();

      // itemTypeId로 이름 조회
      String name = itemTypeRepository.findById(itemTypeId)
          .orElseThrow(() -> new CustomException(MoveItemErrorCode.INVALID_ITEM_TYPE_ID))
          .getItemTypeName();

      Optional<MoveItemBasePrice> existing = basePriceRepository.findByStoreIdAndItemTypeId(storeId,
          itemTypeId);

      if (existing.isPresent()) {
        existing.get().updatePrice(request.getBasePrice());
        log.info("기존 항목 업데이트 - storeId: {}, itemTypeId: {}, newPrice: {}",
            storeId, request.getItemTypeId(), request.getBasePrice());
      } else {
        log.info("신규 항목 저장 - storeId: {}, itemTypeId: {}, basePrice: {}",
            storeId, request.getItemTypeId(), request.getBasePrice());

        MoveItemBasePrice newEntity = MoveItemBasePrice.builder()
            .storeId(storeId)
            .itemTypeId(itemTypeId)
            .itemTypeName(name)
            .basePrice(request.getBasePrice())
            .build();

        basePriceRepository.save(newEntity);
      }

      // 모두 응답 DTO로 반환
      return new SaveItemBasePriceResponse(itemTypeId, name, request.getBasePrice());
    }).toList();

    log.info("기본단가 저장 완료. 응답 {}개", responses.size());
    return responses;
  }

  @Transactional(readOnly = true)
  public List<SaveItemBasePriceResponse> getBasePrices(Long storeId, List<Long> itemTypeIds) {
    List<SaveItemBasePriceResponse> result = basePriceRepository
        .findByStoreIdAndItemTypeIdIn(storeId, itemTypeIds).stream()
        .map(bp -> new SaveItemBasePriceResponse(bp.getItemTypeId(), bp.getItemTypeName(),
            bp.getBasePrice()))
        .collect(Collectors.toList());

    log.info("[BasePrice 조회] 사장님 {}의 itemTypeId {}개에 대한 기본단가 {}건 조회됨",
        storeId, itemTypeIds.size(), result.size());
    return result;
  }

  // 해당 가게 짐 목록 단가 조회
  public List<SaveItemBasePriceResponse> findAllByStoreId(Long userId) {

    Owner owner = ownerRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(StaffErrorCode.OWNER_NOT_FOUND));

    Store store = storeRepository.findByOwnerUserId(owner.getId())
        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    Long storeId = store.getId();

    // 3. Store ID로 기본 단가 조회
    List<MoveItemBasePrice> prices = moveItemBasePriceRepository.findAllByStoreId(storeId);

    return prices.stream()
        .map(SaveItemBasePriceResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<EstimateItemWithExtraChargeResponse> getItemsWithExtrasByEstimateNo(
      Long estimateNo,
      Long userId
  ) {
    Store store = storeRepository.findByOwnerUserId(userId)
        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    Long storeId = store.getId();


    // 2. 해당 견적서의 짐 목록 조회 (견적서 번호 + 가게 ID 필터)
    List<MoveItemPriceByStore> baseItems = moveItemPriceByStoreRepository
        .findByEstimateNoAndStoreId(estimateNo, storeId);

    List<Long> itemTypeIds = baseItems.stream()
        .map(MoveItemPriceByStore::getItemTypeId)
        .toList();

    // 3. 추가금 정보 조회
    List<MoveItemExtraCharge> extras = moveItemExtraChargeRepository
        .findByEstimateNoAndStoreIdAndItemTypeIdIn(estimateNo, storeId, itemTypeIds);
    Map<Long, MoveItemExtraCharge> extraMap = extras.stream()
        .collect(Collectors.toMap(
            MoveItemExtraCharge::getItemTypeId,
            ec -> ec,
            (first, second) -> first // 중복 방지
        ));

    // 4. 기본금 정보 조회
    Map<Long, Integer> basePriceMap = new HashMap<>();
    for (Long itemTypeId : itemTypeIds) {
      int basePrice = moveItemPriceByStoreRepository
          .findByStoreIdAndItemTypeId(storeId, itemTypeId)
          .map(MoveItemPriceByStore::getBasePrice)
          .orElse(0);
      basePriceMap.put(itemTypeId, basePrice);
    }

    List<MoveItems> items = moveItemsRepository.findByEstimateNoAndStoreId(estimateNo, storeId);
    Map<Long, MoveItems> itemMap = items.stream()
        .collect(Collectors.toMap(MoveItems::getItemTypeId, item -> item));



    // 5. 응답 DTO 구성
    return baseItems.stream()
        .map(base -> {
          Long itemTypeId = base.getItemTypeId();

          MoveItems item = itemMap.get(itemTypeId);
          String itemName = item != null ? item.getItemTypeName() : "";
          MoveItemCategory category = item != null ? item.getCategory() : null;

          MoveItemExtraCharge ec = extraMap.get(itemTypeId);
          int extraAmt = ec != null ? ec.getAmount() : 0;
          String reason = ec != null ? ec.getReason() : "";

          return EstimateItemWithExtraChargeResponse.builder()
              .estimateNo(estimateNo)
              .itemTypeId(itemTypeId)
              .itemTypeName(itemName)
              .moveItemCategory(category)
              .basePrice(base.getBasePrice())
              .extraCharge(extraAmt)
              .reason(reason)
              .build();
        })
        .collect(Collectors.toList());
  }



}