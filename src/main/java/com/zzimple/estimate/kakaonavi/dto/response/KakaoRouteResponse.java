package com.zzimple.estimate.kakaonavi.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoRouteResponse {
  private int distance;
  private int duration;

  private List<Mark> marks;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Mark {
    PointType type;
    private double x;
    private double y;
  }

  public enum PointType {
    START, END
  }

  private List<RoutePoint> routePoints;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RoutePoint {
    private double x;           // 경도
    private double y;           // 위도
  }
}
