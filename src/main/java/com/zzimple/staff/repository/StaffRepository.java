package com.zzimple.staff.repository;

import com.zzimple.staff.entity.Staff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
  Optional<Staff> findByStaffId(Long staffId);
  boolean existsByStaffIdAndOwnerId(Long staffId, Long ownerId);

}
