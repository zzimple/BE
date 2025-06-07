package com.zzimple.estimate.guest.repository;

import com.zzimple.estimate.guest.entity.MoveItems;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveItemsRepository extends JpaRepository<MoveItems, Long> {
  List<MoveItems> findByEstimateNo(Long estimateNo);
  void deleteByEstimateNo(Long estimateNo);
}
