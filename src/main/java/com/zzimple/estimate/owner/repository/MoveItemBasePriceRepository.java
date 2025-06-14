package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.MoveItemBasePrice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveItemBasePriceRepository extends JpaRepository<MoveItemBasePrice, Long> {
  Optional<MoveItemBasePrice> findByStoreIdAndItemTypeId(Long storeId, Long itemTypeId);
  List<MoveItemBasePrice> findByStoreIdAndItemTypeIdIn(Long storeId, List<Long> itemTypeIds);
  List<MoveItemBasePrice> findAllByStoreId(Long storeId);
}
