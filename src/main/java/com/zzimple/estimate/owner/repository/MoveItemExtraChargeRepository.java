package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveItemExtraChargeRepository extends JpaRepository<MoveItemExtraCharge, Long> {
  List<MoveItemExtraCharge> findByItemTypeId(Long itemTypeId);
  void deleteByItemTypeId(Long itemTypeId);  List<MoveItemExtraCharge>
  findByEstimateNoAndStoreIdAndItemTypeId(
          Long estimateNo,
          Long storeId,
          Long itemTypeId
      );
}