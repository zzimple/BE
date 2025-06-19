package com.zzimple.staff.repository;

import com.zzimple.owner.enums.StaffScheduleStatus;
import com.zzimple.staff.entity.StaffAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
  Optional<StaffAssignment> findByEstimateNoAndStaffId(Long estimateNo, Long staffId);
  List<StaffAssignment> findByWorkDate(String workDate);
}

