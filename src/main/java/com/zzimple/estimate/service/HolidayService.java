package com.zzimple.estimate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.dto.response.HolidayPreviewResponse;
import com.zzimple.estimate.dto.response.HolidaysaveResponse;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.HolidayErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
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

  private static final Duration TTL = Duration.ofMinutes(30);

  // 공휴일 미리 보기
  public HolidayPreviewResponse previewHoliday(String date) {
    String redisKey = RedisKeyUtil.previewHolidayKey(date);

    // 저장된 캐시 확인
    String cached = redisTemplate.opsForValue().get(redisKey);
    log.info("[HolidayService] 캐시 조회 → 키={}, 값={}", redisKey, cached);

    if (cached != null) {
      String[] parts = cached.split(":", 2);
      return new HolidayPreviewResponse(parts[0], parts.length > 1 ? parts[1] : null);
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
      String json = restTemplate.getForEntity(apiUrl, String.class).getBody();

      JsonNode items = objectMapper
          .readTree(json)
          .path("response")
          .path("body")
          .path("items")
          .path("item");

      if (items.isArray()) {
        for (JsonNode item : items) {
          if (date.equals(item.path("locdate").asText())) {
            isHoliday = item.path("isHoliday").asText("N");
            dateName  = item.path("dateName").asText(null);
            break;
          }
        }
      }
    } catch (JsonProcessingException e) {
      log.warn("[HolidayService] JSON 파싱 오류 - 날짜={}, 오류={}", date, e.getMessage());
      throw new CustomException(HolidayErrorCode.API_CALL_FAILED);
    } catch (Exception e) {
      log.warn("[HolidayService] 외부 API 호출 실패 - 날짜={}, 오류={}", date, e.getMessage());
      throw new CustomException(HolidayErrorCode.API_CALL_FAILED);
    }

    // 주말 처리
    if ("N".equals(isHoliday)) {
      DayOfWeek dow = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")).getDayOfWeek();
      if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
        isHoliday = "Y";
        dateName   = "주말";
        log.info("[HolidayService] 주말 감지 - 요일={}", dow);
      }
    }

    // 캐시 저장
    redisTemplate.opsForValue()
        .set(redisKey, isHoliday + ":" + (dateName != null ? dateName : ""), TTL);

    return new HolidayPreviewResponse(isHoliday, dateName);
  }

  // 이사 예정일 저장
  public HolidaysaveResponse saveMoveDate(UUID draftId, String moveDate) {
    String key = RedisKeyUtil.draftMoveDateKey(draftId);
    redisTemplate.opsForValue().set(key, moveDate, TTL);

    return new HolidaysaveResponse(moveDate);
  }

  // 저장된 이사 예정일 불러오기
  public HolidaysaveResponse getMoveDate(UUID draftId) {
    String key = RedisKeyUtil.draftMoveDateKey(draftId);
    String moveDate = redisTemplate.opsForValue().get(key);
    if (moveDate == null) {
      throw new CustomException(HolidayErrorCode.HOLIDAY_DRAFT_NOT_FOUND);
    }
    return new HolidaysaveResponse(moveDate);
  }
}
