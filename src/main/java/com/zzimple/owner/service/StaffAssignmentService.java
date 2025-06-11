package com.zzimple.owner.service;

import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.owner.dto.response.AssignStaffDateResponse;
import com.zzimple.staff.entity.Staff;
import com.zzimple.staff.entity.StaffAssignment;
import com.zzimple.staff.repository.StaffAssignmentRepository;
import com.zzimple.staff.repository.StaffRepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffAssignmentService {

  private final EstimateRepository estimateRepository;
  private final StaffRepository staffRepository;
  private final StaffAssignmentRepository staffAssignmentRepository;
  private final UserRepository userRepository;

  @Transactional
  public AssignStaffDateResponse assignWithDate(Long estimateNo, Long staffId, LocalDate workDate) {

    estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new EntityNotFoundException("견적 없음 id=" + estimateNo));


    Staff staff = staffRepository.findById(staffId)
        .orElseThrow(() -> new EntityNotFoundException("직원 없음 staffId=" + staffId));

    Long userId = staff.getUserId();

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userId));

    String staffName = user.getUserName();

    // 중복 배정 방지 (필요시)
    staffAssignmentRepository.findByEstimateNoAndStaffId(estimateNo, staffId)
        .ifPresent(a -> { throw new IllegalStateException("이미 배정된 직원입니다."); });

    StaffAssignment assignment = StaffAssignment.builder()
        .estimateNo(estimateNo)
        .staffId(staffId)
        .staffName(staffName)
        .workDate(workDate)
        .build();
    staffAssignmentRepository.save(assignment);

    return AssignStaffDateResponse.builder()
        .staffId(staffId)
        .staffName(staffName)
        .workDate(workDate)
        .build();
  }

  @Transactional
  public AssignStaffDateResponse updateAssignmentDate(Long estimateNo, Long staffId, LocalDate newWorkDate
  ) {

    Staff staff = staffRepository.findById(staffId)
        .orElseThrow(() -> new EntityNotFoundException("직원 없음 staffId=" + staffId));

    Long userId = staff.getUserId();

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userId));

    String staffName = user.getUserName();

    // 1) 기존 배정 조회
    StaffAssignment assignment = staffAssignmentRepository.findByEstimateNoAndStaffId(estimateNo, staffId)
        .orElseThrow(() -> new EntityNotFoundException(
            "배정 내역 없음 estimateNo=" + estimateNo + ", userId=" + userId));

    // 2) 날짜 변경
    assignment.setWorkDate(newWorkDate);

    staffAssignmentRepository.save(assignment);

    // 3) 응답 DTO
    return AssignStaffDateResponse.builder()
        .staffId(staffId)
        .staffName(staffName)
        .workDate(assignment.getWorkDate())
        .build();
  }
}
