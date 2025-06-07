package com.zzimple.estimate.owner.service;

import com.zzimple.estimate.guest.exception.MoveItemErrorCode;
import com.zzimple.estimate.guest.repository.ItemTypeRepository;
import com.zzimple.estimate.owner.dto.request.SaveItemBasePriceRequest;
import com.zzimple.estimate.owner.dto.response.SaveItemBasePriceResponse;
import com.zzimple.estimate.owner.entity.MoveItemBasePrice;
import com.zzimple.estimate.owner.repository.MoveItemBasePriceRepository;
import com.zzimple.global.exception.CustomException;
import java.util.List;
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

  @Transactional
  public List<SaveItemBasePriceResponse> saveOrUpdateAll(Long storeId, List<SaveItemBasePriceRequest> requests) {
    log.info("[BasePrice] 사장님 {}의 기본단가 {}개 저장 시도", storeId, requests.size());

    List<SaveItemBasePriceResponse> responses = requests.stream().map(request -> {
      Long itemTypeId = request.getItemTypeId();

      // itemTypeId로 이름 조회
      String name = itemTypeRepository.findById(itemTypeId)
          .orElseThrow(() -> new CustomException(MoveItemErrorCode.INVALID_ITEM_TYPE_ID))
          .getItemTypeName();

      Optional<MoveItemBasePrice> existing = basePriceRepository.findByStoreIdAndItemTypeId(storeId, itemTypeId);

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
        .map(bp -> new SaveItemBasePriceResponse(bp.getItemTypeId(),  bp.getItemTypeName(), bp.getBasePrice()))
        .collect(Collectors.toList());

    log.info("[BasePrice 조회] 사장님 {}의 itemTypeId {}개에 대한 기본단가 {}건 조회됨",
        storeId, itemTypeIds.size(), result.size());
    return result;
  }

}

