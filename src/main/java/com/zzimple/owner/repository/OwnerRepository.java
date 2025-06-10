package com.zzimple.owner.repository;

import com.zzimple.owner.entity.Owner;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
  Optional<Owner> findByBusinessNumber(String businessNumber);
  Optional<Owner> findByUserId(Long userId);
}