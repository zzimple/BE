package com.zzimple.estimate.gpt.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptResponse {
  private List<ComparisonRow> comparisonTable;
  private String summary;
  private String recommendation;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ComparisonRow {
    private String category;
    private JsonNode estimateA;
    private JsonNode estimateB;
  }
}