package com.zzimple.estimate.guest.repository;

import com.zzimple.estimate.guest.entity.ItemType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {
  List<ItemType> findByItemTypeNameIn(List<String> names);
  List<ItemType> findByItemTypeIdIn(List<Long> ids);
}

