package com.zzimple.estimate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.dto.response.MonthlyHolidayPreviewResponse;
import com.zzimple.estimate.dto.response.HolidaySaveResponse;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.HolidayErrorCode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public List<MonthlyHolidayPreviewResponse> previewMonthlyHolidays(String yearMonth) {
    String cacheKey = String.format("estimate:preview:monthly-holiday:%s", yearMonth);
    String cachedJson = redisTemplate.opsForValue().get(cacheKey);
    if (cachedJson != null) {
      try {
        return objectMapper.readValue(
            cachedJson,
            objectMapper.getTypeFactory().constructCollectionType(
                List.class, MonthlyHolidayPreviewResponse.class));
      } catch (Exception e) {
        log.warn("[HolidayService] 월별 캐시 파싱 오류 - yearMonth={}, error={}", yearMonth, e.getMessage());
      }
    }

    String year = yearMonth.substring(0,4);
    String month = yearMonth.substring(4,6);
    URI apiUrl = UriComponentsBuilder.fromUriString(
            "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
        .queryParam("serviceKey", holiday_api_key)
        .queryParam("solYear", year)
        .queryParam("solMonth", month)
        .queryParam("numOfRows", "100")
        .queryParam("pageNo", "1")
        .queryParam("_type", "json")
        .build(true).toUri();
    log.info("[HolidayService] 월별 요청 URL: {}", apiUrl);
    List<MonthlyHolidayPreviewResponse> result = new ArrayList<>();

    try {
      String json = restTemplate.getForEntity(apiUrl, String.class).getBody();
      JsonNode items = objectMapper.readTree(json)
          .path("response").path("body").path("items").path("item");
      Map<String, String> holidayMap = new HashMap<>();
      if (items.isArray()) {
        for (JsonNode item : items) {
          String date = item.path("locdate").asText();
          String name = item.path("dateName").asText(null);
          holidayMap.put(date, name != null ? name : "");
        }
      }

      YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyyMM"));
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

      for (int day = 1; day <= ym.lengthOfMonth(); day++) {
        String date = ym.atDay(day).format(fmt);
        String isHoliday = holidayMap.containsKey(date) ? "Y" : "N";
        String dateName = holidayMap.get(date);
        DayOfWeek dow = ym.atDay(day).getDayOfWeek();

        if ("N".equals(isHoliday) && (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)) {
          isHoliday = "Y";
          dateName = "주말";
        }

        if ("N".equals(isHoliday)) {
          String lunarResult = checkGoodMoveDay(date);
          if (lunarResult != null) {
            isHoliday = "Y";
            dateName = lunarResult;
          }
        }

        result.add(new MonthlyHolidayPreviewResponse(date, isHoliday, dateName));
      }

      String outJson = objectMapper.writeValueAsString(result);
      redisTemplate.opsForValue().set(cacheKey, outJson, TTL);
    } catch (Exception e) {
      log.warn("[HolidayService] 월별 공휴일 조회 실패 - yearMonth={}, error={}", yearMonth, e.getMessage());
      throw new CustomException(HolidayErrorCode.API_CALL_FAILED);
    }

    return result;
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

    // 공휴일 정보 조회: 월 단위로 가져온 후 해당 날짜 필터링
    String yearMonth = moveDate.substring(0, 6);
    List<MonthlyHolidayPreviewResponse> monthly = previewMonthlyHolidays(yearMonth);
    MonthlyHolidayPreviewResponse matched = monthly.stream()
        .filter(m -> m.getDate().equals(moveDate))
        .findFirst()
        .orElseThrow(() -> new CustomException(HolidayErrorCode.API_CALL_FAILED));

    // Redis에 공휴일 정보 저장
    String holidayInfoKey = RedisKeyUtil.draftMoveHolidayKey(draftId);
    String holidayInfoValue = matched.getHoliday() + ":" + (matched.getDateName() != null ? matched.getDateName() : "");
    redisTemplate.opsForValue().set(holidayInfoKey, holidayInfoValue, TTL);
    log.info("[HolidayService] 공휴일 정보 저장 - draftId={}, holidayInfo={}", draftId, holidayInfoValue);

    // 응답에 공휴일 이름까지 포함하여 반환
    return new HolidaySaveResponse(
        moveDate,
        moveTime,
        matched.getDateName()
    );
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

    // 공휴일 정보 키 조회
    String holidayInfoKey = RedisKeyUtil.draftMoveHolidayKey(draftId);
    log.info("[HolidayService] 공휴일 정보 조회 - draftId={}, 키={}", draftId, holidayInfoKey);
    String holidayInfoValue = redisTemplate.opsForValue().get(holidayInfoKey);
    // holidayInfoValue 포맷: "Y:어린이날" 또는 "N:"
    String dateName = null;
    if (holidayInfoValue != null && holidayInfoValue.contains(":")) {
      String[] parts = holidayInfoValue.split(":", 2);
      // parts[0] = "Y" or "N", parts[1] = 날짜명 or ""
      dateName = parts[1].isEmpty() ? null : parts[1];
    }

    log.info("[HolidayService] 이사일/시간/공휴일명 반환 - draftId={}, 날짜={}, 시간={}, 공휴일명={}",
        draftId, moveDate, moveTime, dateName);

    return new HolidaySaveResponse(
        moveDate,
        moveTime,
        dateName
    );
  }
}
