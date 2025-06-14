package com.zzimple.estimate.kakaonavi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoRouteRequest {
  private String origin;
  private String destination;
}
