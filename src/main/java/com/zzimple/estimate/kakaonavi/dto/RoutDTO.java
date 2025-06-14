package com.zzimple.estimate.kakaonavi.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutDTO {
  private List<SectionDto> sections;

  @Getter
  @Setter
  public static class SectionDto {
    private List<RoadDto> roads;
  }

  @Getter
  @Setter
  public static class RoadDto {
    private List<Double> vertexes;
  }
}
