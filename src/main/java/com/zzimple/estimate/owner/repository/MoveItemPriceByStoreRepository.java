package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.EstimateCalculation;
import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import com.zzimple.estimate.owner.entity.MoveItemPriceByStore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveItemPriceByStoreRepository extends JpaRepository<MoveItemPriceByStore, Long> {
  // 가게의 기본 금액 (estimateNo 없이)
  Optional<MoveItemPriceByStore> findByStoreIdAndItemTypeIdAndEstimateNoIsNull(Long storeId, Long itemTypeId);

  // 견적서에 저장된 금액
  Optional<MoveItemPriceByStore> findByEstimateNoAndStoreIdAndItemTypeId(Long estimateNo, Long storeId, Long itemTypeId);
  Optional<MoveItemPriceByStore> findByStoreIdAndItemTypeId(Long storeId, Long itemTypeId);

  // estimateNo 기준 전체 조회
  List<MoveItemPriceByStore> findByEstimateNoAndStoreId(Long estimateNo, Long storeId);}
