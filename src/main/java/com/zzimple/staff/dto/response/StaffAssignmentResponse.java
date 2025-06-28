package com.zzimple.staff.dto.response;

import com.zzimple.staff.entity.StaffAssignment;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAssignmentResponse {
  private Long staffId;
  private Long estimateNo;
  private LocalDate workDate;
  private String staffName;

  public static StaffAssignmentResponse from(StaffAssignment entity) {
    return new StaffAssignmentResponse(
        entity.getId(),
        entity.getEstimateNo(),
        entity.getWorkDate(),
        entity.getStaffName()
    );
  }
}


