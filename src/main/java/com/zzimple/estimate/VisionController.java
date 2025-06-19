package com.zzimple.estimate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/vision")
public class VisionController {

  @Value("${google.vision.api-key}")
  private String apiKey;

  private final List<String> moveItems = List.of(
      "bed", "desk", "chair", "sofa", "couch", "bookshelf", "table", "lamp", "plant", "tv", "refrigerator"
  );

  @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> analyzeImage(@RequestParam("image") MultipartFile file) throws IOException {
    // 1. 이미지 base64 인코딩
    String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

    // 2. 요청 JSON 생성
    Map<String, Object> image = Map.of("content", base64Image);
    Map<String, Object> feature = Map.of("type", "LABEL_DETECTION");
    Map<String, Object> request = Map.of("image", image, "features", List.of(feature));
    Map<String, Object> requestBody = Map.of("requests", List.of(request));

    // 3. RestTemplate으로 Vision API 호출
    RestTemplate restTemplate = new RestTemplate();
    String url = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(url, httpEntity, Map.class);

    // 4. 라벨 추출 및 필터링
    List<Map<String, Object>> responses = (List<Map<String, Object>>) response.getBody().get("responses");
    List<Map<String, Object>> labels = (List<Map<String, Object>>) responses.get(0).get("labelAnnotations");

    List<String> detectedItems = labels.stream()
        .map(label -> ((String) label.get("description")).toLowerCase())
        .filter(moveItems::contains)
        .distinct()
        .toList();

    Map<String, Object> result = new HashMap<>();
    result.put("detectedItems", detectedItems);
    return ResponseEntity.ok(result);
  }
}