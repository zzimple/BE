package com.zzimple.estimate.gpt.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Body {
  private String model;
  private List<Message> messages;
}