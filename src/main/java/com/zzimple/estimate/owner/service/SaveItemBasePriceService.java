package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.entity.MoveItems;
import com.zzimple.estimate.guest.exception.MoveItemErrorCode;
import com.zzimple.estimate.guest.repository.ItemTypeRepository;
import com.zzimple.estimate.guest.repository.MoveItemsRepository;
import com.zzimple.estimate.owner.dto.request.SaveItemBasePriceRequest;
import com.zzimple.estimate.owner.dto.response.EstimateItemWithExtraChargeResponse;
import com.zzimple.estimate.owner.dto.response.SaveItemBasePriceResponse;
import com.zzimple.estimate.owner.entity.MoveItemBasePrice;
import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import com.zzimple.estimate.owner.repository.MoveItemBasePriceRepository;
import com.zzimple.estimate.owner.repository.MoveItemExtraChargeRepository;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.exception.StoreErrorCode;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.staff.exception.StaffErrorCode;
import com.zzimple.user.repository.UserRepository;
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
    // 1. 유저 → 사장님 → 가게 ID 조회
    Owner owner = ownerRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(StaffErrorCode.OWNER_NOT_FOUND));
    Store store = storeRepository.findByOwnerUserId(owner.getId())
        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));
    Long storeId = store.getId();

    // 2. 해당 견적서의 짐 목록 조회 (견적서 번호 + 가게 ID 필터)
    List<MoveItems> items = moveItemsRepository.findByEstimateNoAndStoreId(estimateNo, storeId);
    List<Long> itemTypeIds = items.stream()
        .map(MoveItems::getItemTypeId)
        .toList();

    // 3. 추가금 정보 조회
    List<MoveItemExtraCharge> extras = moveItemExtraChargeRepository
        .findByEstimateNoAndItemTypeIdIn(estimateNo, itemTypeIds);
    Map<Long, MoveItemExtraCharge> extraMap = extras.stream()
        .collect(Collectors.toMap(
            MoveItemExtraCharge::getItemTypeId,
            ec -> ec,
            (first, second) -> first // 중복 방지
        ));

    // 4. 응답 DTO 구성
    return items.stream()
        .map(item -> {
          MoveItemExtraCharge ec = extraMap.get(item.getItemTypeId());
          int extraAmt = ec != null ? ec.getAmount() : 0;
          // [수정] reason이 null일 때 빈 문자열로 처리
          String reason = (ec != null && ec.getReason() != null)
              ? ec.getReason()
              : "";

          return EstimateItemWithExtraChargeResponse.builder()
              .estimateNo(estimateNo)
              .itemTypeId(item.getItemTypeId())
              .itemTypeName(item.getItemTypeName())
              .moveItemCategory(item.getCategory())
              .basePrice(item.getBasePrice())
              .extraCharge(extraAmt)
              .reason(reason)
              .build();
        })
        .collect(Collectors.toList());
  }


}