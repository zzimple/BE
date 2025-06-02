package com.zzimple.estimate.guest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.guest.dto.response.MonthlyHolidayPreviewResponse;
import com.zzimple.estimate.guest.dto.response.HolidaySaveResponse;
import com.zzimple.global.config.RedisKeyUtil;
import com.zzimple.global.exception.CustomException;
import com.zzimple.estimate.guest.exception.HolidayErrorCode;
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

  private static final Duration TTL = Duration.ofHours(1);

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

    // LocalDate 파싱 (중복 제거용)
    LocalDate parsedDate = LocalDate.parse(moveDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

    // 공휴일 정보 조회: 월 단위로 가져온 후 해당 날짜 필터링
    String yearMonth = moveDate.substring(0, 6);
    List<MonthlyHolidayPreviewResponse> monthly = previewMonthlyHolidays(yearMonth);
    MonthlyHolidayPreviewResponse matched = monthly.stream()
        .filter(m -> m.getDate().equals(moveDate))
        .findFirst()
        .orElseThrow(() -> new CustomException(HolidayErrorCode.API_CALL_FAILED));

    // === [수정 ②] 공휴일 여부 및 이름 Redis 저장 ===
    boolean isHoliday = matched != null && "Y".equals(matched.getHoliday());
    String holidayName = matched != null ? matched.getDateName() : "";

    redisTemplate.opsForValue().set(RedisKeyUtil.draftIsHolidayKey(draftId), String.valueOf(isHoliday), TTL);
    redisTemplate.opsForValue().set(RedisKeyUtil.draftHolidayNameKey(draftId), holidayName, TTL);

    // 손 없는 날 여부 저장
    String goodDayLabel = checkGoodMoveDay(moveDate);
    boolean isGoodDay = goodDayLabel != null;
    redisTemplate.opsForValue().set(RedisKeyUtil.draftIsGoodDayKey(draftId), String.valueOf(isGoodDay), TTL);

    // 주말 여부 저장
    DayOfWeek dayOfWeek = parsedDate.getDayOfWeek();
    boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
    redisTemplate.opsForValue().set(RedisKeyUtil.draftIsWeekendKey(draftId), String.valueOf(isWeekend), TTL);

    log.info("[HolidayService] 공휴일 정보 저장 - draftId={}, isHoliday={}, holidayName={}", draftId, isHoliday, holidayName);

    // 응답에 공휴일 이름까지 포함하여 반환
    return new HolidaySaveResponse(
        moveDate,
        moveTime,
        holidayName,
        isHoliday,
        isGoodDay,
        isWeekend
    );
  }

  // 저장된 이사 예정일 불러오기
  public HolidaySaveResponse getMoveDate(UUID draftId) {
    // 날짜
    String moveDate = redisTemplate.opsForValue().get(RedisKeyUtil.draftMoveDateKey(draftId));
    log.info("[HolidayService] 이사일 조회 - draftId={}, moveDate={}", draftId, moveDate);

    // 시간
    String moveTime = redisTemplate.opsForValue().get(RedisKeyUtil.draftMoveTimeKey(draftId));
    log.info("[HolidayService] 이사시간 조회 - draftId={}, moveTime={}", draftId, moveTime);

    // 공휴일 여부
    String isHolidayStr = redisTemplate.opsForValue().get(RedisKeyUtil.draftIsHolidayKey(draftId));
    boolean isHoliday = Boolean.parseBoolean(isHolidayStr);

    // 공휴일 이름
    String holidayName = redisTemplate.opsForValue().get(RedisKeyUtil.draftHolidayNameKey(draftId));

    // 손 없는 날 여부
    String isGoodDayStr = redisTemplate.opsForValue().get(RedisKeyUtil.draftIsGoodDayKey(draftId));
    boolean isGoodDay = Boolean.parseBoolean(isGoodDayStr);

    // 주말 여부
    String isWeekendStr = redisTemplate.opsForValue().get(RedisKeyUtil.draftIsWeekendKey(draftId));
    boolean isWeekend = Boolean.parseBoolean(isWeekendStr);

    // 로그 출력
    log.info("[HolidayService] 이사일 정보 반환 - draftId={}, 날짜={}, 시간={}, 공휴일={}, 손없는날={}, 주말={}, 공휴일명={}",
        draftId, moveDate, moveTime, isHoliday, isGoodDay, isWeekend, holidayName);

    return new HolidaySaveResponse(
        moveDate,
        moveTime,
        holidayName,
        isHoliday,
        isGoodDay,
        isWeekend
    );
  }
}
