package com.zzimple.owner.store.service;

import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.owner.store.exception.StoreErrorCode;
import com.zzimple.owner.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreQueryService {
  private final StoreRepository storeRepository;

  public Long getStoreIdByOwnerUserId(Long userId) {
    return storeRepository.findByOwnerId(userId)
        .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND))
        .getId();
  }
}
