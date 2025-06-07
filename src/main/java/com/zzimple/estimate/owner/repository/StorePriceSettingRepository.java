package com.zzimple.estimate.owner.repository;

import com.zzimple.estimate.owner.entity.StorePriceSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorePriceSettingRepository extends JpaRepository<StorePriceSetting, Long> {
  Optional<StorePriceSetting> findByStoreId(Long storeId);
}
