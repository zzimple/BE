package com.zzimple.estimate.gpt.dto.request;

import com.zzimple.estimate.gpt.dto.EstimateDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatGptRequest {
  private EstimateDto estimateA;
  private EstimateDto estimateB;
}