package com.zzimple.staff.repository;

import com.zzimple.owner.enums.StaffScheduleStatus;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.enums.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
  boolean existsByUserIdAndOwnerId(Long userId, Long ownerId);
  List<Staff> findByOwnerIdAndStatus(Long ownerId, Status status);
  Optional<Staff> findByStaffId(Long staffId);
  List<Staff> findByStoreId(Long storeId);
  Optional<Staff> findByUserId(Long userId);
}
