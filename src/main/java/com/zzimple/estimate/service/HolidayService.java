package com.zzimple.estimate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.dto.response.HolidayPreviewResponse;
import com.zzimple.estimate.dto.response.HolidaySaveResponse;
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
  private String holiday_api_key;

  @Value("${api.lunar.key}")
  private String lunar_api_key;

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
          .queryParam("serviceKey", holiday_api_key)
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

    if ("N".equals(isHoliday)) {
      String lunarResult = checkGoodMoveDay(date);
      log.info("[HolidayService] 손 없는 날 여부 확인 결과 - date={}, result={}", date, lunarResult);
      if (lunarResult != null) {
        isHoliday = "Y";
        dateName = lunarResult;
      }
    }

    // 캐시 저장
    redisTemplate.opsForValue()
        .set(redisKey, isHoliday + ":" + (dateName != null ? dateName : ""), TTL);

    return new HolidayPreviewResponse(isHoliday, dateName);
  }

  private String checkGoodMoveDay(String date) {
    try {
      LocalDate solar = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));

      URI lunarApi = UriComponentsBuilder
          .fromUriString("https://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getLunCalInfo")
          .queryParam("ServiceKey", lunar_api_key)
          .queryParam("solYear", solar.getYear())
          .queryParam("solMonth", String.format("%02d", solar.getMonthValue()))
          .queryParam("solDay", String.format("%02d", solar.getDayOfMonth()))
          .queryParam("_type", "json")
          .build(true)
          .toUri();

      String lunarJson = restTemplate.getForObject(lunarApi, String.class);
      JsonNode item = objectMapper.readTree(lunarJson)
          .path("response").path("body").path("items").path("item");

      int lunarDay = item.path("lunDay").asInt();

      if (lunarDay == 9 || lunarDay == 10 ||
          lunarDay == 19 || lunarDay == 20 ||
          lunarDay == 29 || lunarDay == 30) {
        log.info("[HolidayService] 손 없는 날 감지 - lunarDay={} (양력 {})", lunarDay, date);
        return "손 없는 날";
      }

    } catch (Exception e) {
      log.warn("[HolidayService] 손 없는 날 확인 실패 - date={}, error={}", date, e.getMessage());
    }
    return null;
  }


  // 이사 예정일 저장
  public HolidaySaveResponse saveMoveDate(UUID draftId, String moveDate, String moveTime) {
    // 날짜 저장
    String dateKey = RedisKeyUtil.draftMoveDateKey(draftId);
    redisTemplate.opsForValue().set(dateKey, moveDate, TTL);
    log.info("[HolidayService] 이사 날짜 저장 - draftId={}, moveDate={}", draftId, moveDate);

    // 시간 저장
    String timeKey = RedisKeyUtil.draftMoveTimeKey(draftId);
    redisTemplate.opsForValue().set(timeKey, moveTime, TTL);
    log.info("[HolidayService] 이사 시간 저장 - draftId={}, moveTime={}", draftId, moveTime);

    return new HolidaySaveResponse(moveDate, moveTime);
  }

  // 저장된 이사 예정일 불러오기
  public HolidaySaveResponse getMoveDate(UUID draftId) {
    // 날짜 키 조회
    String dateKey = RedisKeyUtil.draftMoveDateKey(draftId);
    log.info("[HolidayService] 이사일 조회 - draftId={}, 키={}", draftId, dateKey);
    String moveDate = redisTemplate.opsForValue().get(dateKey);

    // 시간 키 조회
    String timeKey = RedisKeyUtil.draftMoveTimeKey(draftId);
    log.info("[HolidayService] 이사시간 조회 - draftId={}, 키={}", draftId, timeKey);
    String moveTime = redisTemplate.opsForValue().get(timeKey);

    if (moveDate == null || moveTime == null) {
      log.warn("[HolidayService] 이사일 또는 이사시간 미발견 - 날짜키={}, 시간키={}", dateKey, timeKey);
      throw new CustomException(HolidayErrorCode.HOLIDAY_DRAFT_NOT_FOUND);
    }
    log.info("[HolidayService] 이사일/시간 반환 - draftId={}, 날짜={}, 시간={}", draftId, moveDate, moveTime);
    return new HolidaySaveResponse(moveDate, moveTime);
  }
}
