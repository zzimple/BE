package com.zzimple.estimate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.dto.response.HolidayCheckResponse;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.HolidayErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

  @Value("${api.holiday.key}")
  private String apiKey;

  private final StringRedisTemplate redisTemplate;
  private final RestTemplate restTemplate = new RestTemplate();

  // JSON 파싱을 위한 ObjectMapper
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final Duration TTL = Duration.ofMinutes(1);  // 개발용이라 1분이라고 정함.


  public HolidayCheckResponse checkHoliday(UUID draftId, String date) {
    String redisKey = RedisKeyUtil.draftHolidayKey(draftId, date);

    // 저장된 캐시 확인
    String cached = redisTemplate.opsForValue().get(redisKey);
    log.info("[HolidayService] 캐시 조회 → 키={}, 값={}", redisKey, cached);

    // 저장된 캐시가 있다면 바로 반환 (API 호출 x)
    if (cached != null) {
      String[] parts = cached.split(":", 2);
      return new HolidayCheckResponse(parts[0], parts.length > 1 ? parts[1] : null);
    }

    // 디폴트 값
    String isHoliday = "N";
    String dateName = null;

    try {
      URI apiUrl = UriComponentsBuilder
          .fromUriString("https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
          .queryParam("serviceKey", apiKey)
          .queryParam("solYear", date.substring(0, 4))
          .queryParam("solMonth", date.substring(4, 6))
          .queryParam("numOfRows", "100")
          .queryParam("pageNo", "1")
          .queryParam("_type", "json")
          .build(true)
          .toUri();

      log.info("[HolidayService] 요청 URL: {}", apiUrl);

      // API 호출
      ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
      String json = response.getBody();

      log.info("[HolidayService] raw JSON:\n{}", json);

      // JSON 파싱
      JsonNode root = objectMapper.readTree(json);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      // 공휴일 목록에서 해당 날짜가 존재하는지 확인
      if (items.isArray()) {
        for (JsonNode item : items) {
          String locdate = item.path("locdate").asText();
          if (locdate.equals(date)) {
            isHoliday = item.path("isHoliday").asText("N");
            dateName = item.path("dateName").asText(null);
            break;
          }
        }
      }
    } catch (Exception e) {
      log.warn("[HolidayCheck] 공휴일 API 호출 실패 - date: {}, 이유: {}", date, e.getMessage());
      throw new CustomException(HolidayErrorCode.API_CALL_FAILED);
    }

    // 공휴일이 아니면 주말 체크
    if (isHoliday.equals("N")) {
      LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
      DayOfWeek dayOfWeek = localDate.getDayOfWeek();
      if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
        isHoliday = "Y";
        dateName = "주말";
        log.info("[HolidayService] 주말입니다 - {}", dayOfWeek);
      }
    }

    redisTemplate.opsForValue().set(redisKey, isHoliday + ":" + (dateName != null ? dateName : ""), TTL);
    log.info("[HolidayService] 최종 결과 - isHoliday: {}, dateName: {}", isHoliday, dateName);

    return new HolidayCheckResponse(isHoliday, dateName);
  }

  // 불러오기
  public HolidayCheckResponse getHolidayInfo(UUID draftId, String date) {
    String redisKey = RedisKeyUtil.draftHolidayKey(draftId, date);
    String cached = redisTemplate.opsForValue().get(redisKey);

    log.info("[HolidayService] 공휴일 정보 불러오기 - 키={}, 값={}", redisKey, cached);

    if (cached == null) {
      throw new CustomException(HolidayErrorCode.HOLIDAY_DRAFT_NOT_FOUND);
    }

    String[] parts = cached.split(":", 2);
    String isHoliday = parts[0];
    String dateName = parts.length > 1 ? parts[1] : null;

    return new HolidayCheckResponse(isHoliday, dateName);
  }

}
