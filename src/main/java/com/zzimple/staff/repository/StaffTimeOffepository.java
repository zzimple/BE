package com.zzimple.staff.repository;

import com.zzimple.staff.entity.StaffTimeOff;
import com.zzimple.staff.enums.Status;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffTimeOffepository extends JpaRepository<StaffTimeOff, Long> {
  Page<StaffTimeOff> findByStaffId(Long staffId, Pageable pageable);
  List<StaffTimeOff> findAllByStoreIdAndStatus(Long storeId, Status status);
  // 특정 직원이, 특정 날짜에, 승인된(Approved) 휴가 기간에 있는지 존재 여부만 확인
  boolean existsByStaffIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
      Long staffId,
      Status status,
      LocalDate startDate,
      LocalDate endDate
  );

  List<StaffTimeOff> findByStaffIdAndStatusAndEndDateGreaterThanEqualAndStartDateLessThanEqual(
      Long staffId,
      Status status,
      LocalDate startDate,
      LocalDate endDate
  );
}
