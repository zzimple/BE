package com.zzimple.staff.repository;

import com.zzimple.staff.entity.Staff;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
  boolean existsByUserIdAndOwnerId(Long userId, Long ownerId);
  List<Staff> findByOwnerId(Long ownerId);
  List<Staff> findByStoreId(Long storeId);
  Optional<Staff> findByUserId(Long userId);
}
