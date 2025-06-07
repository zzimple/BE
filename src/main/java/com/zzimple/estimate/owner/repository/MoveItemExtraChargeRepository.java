package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.MoveItemExtraCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveItemExtraChargeRepository extends JpaRepository<MoveItemExtraCharge, Long> {
  List<MoveItemExtraCharge> findByMoveItemId(Long moveItemId);
  void deleteByMoveItemId(Long moveItemId);
}
