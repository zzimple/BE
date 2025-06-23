package com.zzimple.estimate.guest.vision.service;

import com.zzimple.estimate.guest.entity.ItemType;
import com.zzimple.estimate.guest.repository.ItemTypeRepository;
import com.zzimple.estimate.guest.vision.entity.UserPreferredItem;
import com.zzimple.estimate.guest.vision.repository.UserPreferredItemRepository;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // 생성자 자동 생성
public class PreferredItemService {

  private final ItemTypeRepository itemTypeRepository;
  private final UserPreferredItemRepository preferredItemRepository;

  /**
   * Vision API 결과로 감지된 항목들을 사용자 선호 목록으로 저장합니다.
   */
  @Transactional
  public void saveDetectedItems(Long userId, List<String> itemNames) {
    List<ItemType> items = itemTypeRepository.findByItemTypeNameIn(itemNames);
    for (ItemType item : items) {
      Long itemTypeId = item.getItemTypeId(); // ← Long
      if (!preferredItemRepository.existsByUserIdAndItemTypeId(userId, itemTypeId)) {
        preferredItemRepository.save(new UserPreferredItem(userId, itemTypeId));
      }
    }
  }

  @Transactional(readOnly = true)
  public List<ItemType> getPreferredItems(Long userId) {
    List<UserPreferredItem> records = preferredItemRepository.findAllByUserId(userId);
    List<Long> itemTypeIds = records.stream()
        .map(UserPreferredItem::getItemTypeId)
        .toList();
    return itemTypeRepository.findByItemTypeIdIn(itemTypeIds);
  }

}
