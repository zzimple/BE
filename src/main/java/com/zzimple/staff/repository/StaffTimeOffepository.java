package com.zzimple.staff.repository;

import com.zzimple.staff.entity.StaffTimeOff;
import com.zzimple.staff.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffTimeOffepository
    extends JpaRepository<StaffTimeOff, Long> {
  Page<StaffTimeOff> findByStoreIdAndStatus(Long storeId, Status status, Pageable pageable);}
