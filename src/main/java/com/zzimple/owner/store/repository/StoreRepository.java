package com.zzimple.owner.store.repository;

import com.zzimple.owner.store.entity.Store;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, Long> {
  @Query("""
    SELECT s
    FROM Store s
    JOIN Owner o ON s.ownerId = o.id
    WHERE o.userId = :userId
  """)
  Optional<Store> findByOwnerUserId(@Param("userId") Long userId);
  boolean existsByIdAndOwnerId(Long id, Long ownerId);
}

