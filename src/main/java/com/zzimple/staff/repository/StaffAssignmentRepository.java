package com.zzimple.staff.repository;

import com.zzimple.owner.enums.StaffScheduleStatus;
import com.zzimple.staff.entity.StaffAssignment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
  Optional<StaffAssignment> findByEstimateNoAndStaffId(Long estimateNo, Long staffId);

  // 🔽 LocalDate 타입으로 수정
  List<StaffAssignment> findByWorkDate(LocalDate workDate);

  // 🔽 LocalDate 범위 검색으로 수정
  List<StaffAssignment> findAllByStaffIdAndWorkDateBetween(
      Long staffId, LocalDate startDate, LocalDate endDate
  );

}

