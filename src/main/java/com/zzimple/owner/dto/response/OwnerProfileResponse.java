package com.zzimple.owner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerProfileResponse {
  private String name;
  private String login_id;
  private String email;

  private String roadFullAddr;
  private String roadAddrPart1;
  private String addrDetail;
  private String zipNo;

  private Long storeId;
}