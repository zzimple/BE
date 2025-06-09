package com.zzimple.estimate.guest.repository;

import com.zzimple.estimate.guest.entity.MoveItems;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MoveItemsRepository extends JpaRepository<MoveItems, Long> {
  List<MoveItems> findByEstimateNo(Long estimateNo);
  void deleteByEstimateNo(Long estimateNo);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("""
    UPDATE MoveItems m
    SET m.basePrice = :basePrice,
        m.storeId = :storeId
    WHERE m.estimateNo = :estimateNo
      AND (m.storeId IS NULL OR m.storeId = :storeId)
      AND m.itemTypeId = :itemTypeId
      AND (m.basePrice IS NULL OR m.basePrice <> :basePrice)
""")
  int updateBasePriceIfChanged(
      @Param("estimateNo") Long estimateNo,
      @Param("storeId") Long storeId,
      @Param("itemTypeId") Long itemTypeId,
      @Param("basePrice") Integer basePrice
  );


  @Query("SELECT m.id FROM MoveItems m WHERE m.estimateNo = :estimateNo AND m.itemTypeId = :itemTypeId")
  Long findIdByEstimateNoAndItemTypeIdOnly(
      @Param("estimateNo") Long estimateNo,
      @Param("itemTypeId") Long itemTypeId
  );
}
