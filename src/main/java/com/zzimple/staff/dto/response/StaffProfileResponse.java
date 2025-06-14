package com.zzimple.staff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaffProfileResponse {
  private String name;
  private String login_id;
  private String email;
  private String OwnerName;
  private String StoreName;
  private String OwnerPhoneNum;
}
