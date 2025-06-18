package com.zzimple.estimate.gpt.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zzimple.estimate.gpt.dto.request.ChatGptRequest;

public class GptSystemPromptFormatter {


  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

  public static String buildPrompt(ChatGptRequest request) {
    String aJson = toJson(request.getEstimateA());
    String bJson = toJson(request.getEstimateB());
    StringBuilder sb = new StringBuilder();

    sb.append("JSON만 출력하세요. 추가 설명을 포함하지 마세요.\n")
        .append("다음 두 개의 이사 견적서를 아래 기준으로 비교해 주세요:\n")
        .append("1. 트럭 수 및 총 비용 (truckCount, truckTotalPrice)\n")
        .append("2. 물품별 추가 요금 여부 및 사유 (itemExtraCharges)\n")
        .append("3. 견적서에 추가로 반영된 항목 및 금액 (extraCharges)\n")
        .append("4. 공휴일/손 없는 날/주말 요금 포함 여부 (holidayCharge, goodDayCharge, weekendCharge)\n")
        .append("5. 사장님의 메시지 (ownerMessage) — 응대 태도, 전문성, 책임감 등 평가\n\n")
        .append("단순히 가격이 싼 쪽을 추천하지 말고, 현실적인 판단이 잘 반영된 쪽을 추천해줘.\n")
        .append("각 항목마다 비교 분석하고, 마지막에 요약과 추천을 제시해줘.\n\n")
        .append("견적서 A:\n").append(aJson).append("\n\n")
        .append("견적서 B:\n").append(bJson).append("\n\n")
        .append("반드시 아래 JSON 구조만 응답하세요:\n")
        .append("{\n")
        .append("  \"comparisonTable\": [\n")
        .append("    { \"category\": \"<항목>\", \"estimateA\": <A 값>, \"estimateB\": <B 값> }\n")
        .append("  ],\n")
        .append("  \"summary\": \"<비교 요약>\",\n")
        .append("  \"recommendation\": \"<추천 문구>\"\n")
        .append("}");

    return sb.toString();
  }

  // 객체를 json 문자열로 반환
  private static String toJson(Object obj) {
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (Exception e) {
      return obj.toString();
    }
  }
}
