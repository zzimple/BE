package com.zzimple.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {
  private Long id;
  private String userName;
  private String phoneNumber;
  private String email;
  private String loginId;
}