package com.zzimple.estimate.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
  private String role;    // 예: "user", "system"
  private String content; // 사용자 입력 또는 시스템 메시지
}