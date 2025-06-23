package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveItemExtraChargeRepository extends JpaRepository<MoveItemExtraCharge, Long> {
  // 여러 itemTypeId에 대해 한 번에 조회
  List<MoveItemExtraCharge> findByEstimateNoAndStoreIdAndItemTypeIdIn(
      Long estimateNo,
      Long storeId,
      List<Long> itemTypeIds
  );

  // 단일 itemTypeId에 대해 조회
  List<MoveItemExtraCharge> findByEstimateNoAndStoreIdAndItemTypeId(
      Long estimateNo,
      Long storeId,
      Long itemTypeId
  );

  void deleteByEstimateNoAndStoreIdAndItemTypeId(Long estimateNo, Long storeId, Long itemTypeId);
  List<MoveItemExtraCharge> findByEstimateNoAndStoreId(
      Long estimateNo,
      Long storeId
  );}