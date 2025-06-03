package com.zzimple.owner.repository;

import com.zzimple.owner.entity.Owner;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
  // loginId 대신 businessNumber 로 조회
  Optional<Owner> findByBusinessNumber(String businessNumber);
  Optional<Owner> findByUserId(Long userId);
}