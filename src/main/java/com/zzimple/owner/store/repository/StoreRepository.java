package com.zzimple.owner.store.repository;

import com.zzimple.owner.store.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
  Optional<Store> findByOwnerId(Long ownerId);  // ownerId는 FK 아님, 인덱스로 검색
}

