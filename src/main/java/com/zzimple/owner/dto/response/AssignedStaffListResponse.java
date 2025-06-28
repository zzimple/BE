package com.zzimple.owner.dto.response;

import com.zzimple.staff.dto.response.StaffAssignmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AssignedStaffListResponse {
  private long count;
  private List<StaffAssignmentResponse> staffList;
}
