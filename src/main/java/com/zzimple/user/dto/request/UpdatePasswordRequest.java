package com.zzimple.user.dto.request;

import lombok.Getter;

@Getter
public class UpdatePasswordRequest {
  private String currentPassword;
  private String newPassword;
}
