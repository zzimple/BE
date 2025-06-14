package com.zzimple.estimate.kakaonavi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoRouteInfo {
  private int distance;  // meter
  private int duration;  // seconds
}