package com.zzimple.estimate.guest.vision.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.guest.entity.ItemType;
import com.zzimple.estimate.guest.vision.service.PreferredItemService;
import com.zzimple.global.dto.BaseResponse;
import com.zzimple.global.jwt.CustomUserDetails;
import java.time.Duration;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/vision")
public class VisionController {

  @Value("${google.vision.api-key}")
  private String apiKey;

  private final PreferredItemService preferredItemService;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  private final Map<String, String> visionLabelMap = Map.ofEntries(
      Map.entry("bed", "침대"),
      Map.entry("sofa", "쇼파"),
      Map.entry("couch", "쇼파"),
      Map.entry("chair", "의자"),
      Map.entry("desk", "책상"),
      Map.entry("table", "테이블/식탁"),
      Map.entry("bookshelf", "책장"),
      Map.entry("lamp", "조명기구"),
      Map.entry("plant", "화분"),
      Map.entry("tv", "TV"),
      Map.entry("refrigerator", "냉장고"),
      Map.entry("microwave", "전자레인지"),
      Map.entry("washing machine", "세탁기"),
      Map.entry("air conditioner", "에어컨"),
      Map.entry("mirror", "거울"),
      Map.entry("curtain", "커튼"),
      Map.entry("fan", "선풍기")
  );

  @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> analyzeImage(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam("image") MultipartFile file) throws IOException {

    log.info("📸 이미지 base64 인코딩 시작");
    // 1. 이미지 base64 인코딩
    String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

    // 2. 요청 JSON 생성
    Map<String, Object> image = Map.of("content", base64Image);
    Map<String, Object> feature = Map.of("type", "LABEL_DETECTION");
    Map<String, Object> request = Map.of("image", image, "features", List.of(feature));
    Map<String, Object> requestBody = Map.of("requests", List.of(request));

    log.info("📤 Vision API 요청 전송 준비 완료");

    // 3. RestTemplate으로 Vision API 호출
    RestTemplate restTemplate = new RestTemplate();
    String url = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(url, httpEntity, Map.class);

    log.info("✅ Vision API 응답 수신 완료");

    // 4. 라벨 추출 및 필터링
    List<Map<String, Object>> responses = (List<Map<String, Object>>) response.getBody().get("responses");
    List<Map<String, Object>> labels = (List<Map<String, Object>>) responses.get(0).get("labelAnnotations");

    log.info("🔍 감지된 라벨 목록:");
    for (Map<String, Object> label : labels) {
      log.info(" - {} (score: {})", label.get("description"), label.get("score"));
    }

    List<String> detectedItems = labels.stream()
        .map(label -> ((String) label.get("description")).toLowerCase())
        .filter(visionLabelMap::containsKey)
        .map(visionLabelMap::get)
        .distinct()
        .toList();

    log.info("🎯 감지 항목 분석 완료: {}", detectedItems);

    // ✅ Redis에 저장 (TTL: 30분)
    String redisKey = "vision:detected:" + user.getUserId();
    String json = objectMapper.writeValueAsString(detectedItems);
    redisTemplate.opsForValue().set(redisKey, json, Duration.ofMinutes(30));

    log.info("📥 Redis 저장 완료 - Key: {}", redisKey);


    Map<String, Object> result = new HashMap<>();
    result.put("detectedItems", detectedItems);
    return ResponseEntity.ok(result);
  }

  // ✅ Vision 감지 결과 저장
  // ✅ Vision 감지 결과 저장
  @PostMapping("/save")
  public ResponseEntity<BaseResponse<String>> savePreferredItems(
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    Long userId = user.getUserId();
    String redisKey = "vision:detected:" + userId;

    String json = redisTemplate.opsForValue().get(redisKey); // Redis에서 문자열로 꺼냄

    if (json == null || json.isEmpty()) {
      return ResponseEntity.badRequest().body(
          BaseResponse.<String>builder()
              .success(false)
              .message("감지된 항목이 없습니다. 먼저 이미지를 분석해주세요.")
              .data(null)
              .build()
      );
    }

    List<String> items;
    try {
      items = objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      log.error("❌ Redis 감지 항목 역직렬화 실패", e);
      return ResponseEntity.internalServerError().body(
          BaseResponse.<String>builder()
              .success(false)
              .message("Redis 항목 역직렬화 중 오류 발생")
              .data(null)
              .build()
      );
    }

    log.info("💾 사용자 {} 감지 항목 저장 요청: {}", userId, items);
    preferredItemService.saveDetectedItems(userId, items);

    redisTemplate.delete(redisKey);
    log.info("🧹 Redis 키 삭제 완료 - {}", redisKey);

    return ResponseEntity.ok(
        BaseResponse.<String>builder()
            .success(true)
            .message("감지된 항목 저장 완료")
            .data("saved")
            .build()
    );
  }



  // ✅ 마이페이지 - 감지된 짐 목록 조회
  @GetMapping
  public ResponseEntity<BaseResponse<List<ItemType>>> getPreferredItems(
      @AuthenticationPrincipal CustomUserDetails user
  ) {
    Long userId = user.getUserId();

    List<ItemType> items = preferredItemService.getPreferredItems(userId);
    log.info("📦 사용자 {}의 선호 항목 조회: {}개", userId, items.size());
    return ResponseEntity.ok(
        BaseResponse.<List<ItemType>>builder()
            .success(true)
            .message("선호 항목 조회 성공")
            .data(items)
            .build()
    );
  }
}