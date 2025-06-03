package com.zzimple.owner.store.repository;

import com.zzimple.owner.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
