package com.zzimple.estimate.guest.vision.repository;

import com.zzimple.estimate.guest.vision.entity.UserPreferredItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferredItemRepository extends JpaRepository<UserPreferredItem, Long> {
  boolean existsByUserIdAndItemTypeId(Long userId, Long itemTypeId);
  List<UserPreferredItem> findAllByUserId(Long userId);
}
