package com.zzimple.estimate.kakaonavi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzimple.estimate.coordinate.CoordinateConverter;
import com.zzimple.estimate.guest.entity.Address;
import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.kakaonavi.dto.request.KakaoRouteRequest;
import com.zzimple.estimate.kakaonavi.dto.response.KakaoRouteResponse;
import com.zzimple.estimate.kakaonavi.dto.response.KakaoRouteResponse.Mark;
import com.zzimple.estimate.kakaonavi.dto.response.KakaoRouteResponse.RoutePoint;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoNaviService {

  @Value("${kakao.rest-api-key}")
  private String kakaoRestApiKey;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public KakaoRouteResponse getRoute(KakaoRouteRequest request) {
    try {
      String originCoords = request.getOrigin();
      String destCoords = request.getDestination();

      // 요청 URL 로그
      String url = UriComponentsBuilder.newInstance()
          .scheme("https")
          .host("apis-navi.kakaomobility.com")
          .path("/v1/directions")
          .queryParam("origin", originCoords)
          .queryParam("destination", destCoords)
          .build(false)
          .toUriString();

      log.info("📡 카카오 내비 요청 URL: {}", url);

      // 인증 헤더
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
      headers.set("Content-Type", "application/json");
      headers.set("Accept", "application/json");
      HttpEntity<?> entity = new HttpEntity<>(headers);

      // API 호출
      ResponseEntity<String> response = restTemplate.exchange(
          url, HttpMethod.GET, entity, String.class);

      System.out.println(response.getBody());
      // 응답 본문 로그
      log.debug("📥 카카오 응답 원문: {}", response.getBody());

      // JSON 파싱
      JsonNode body = objectMapper.readTree(response.getBody());

      JsonNode routes = body.path("routes");
      if (!routes.isArray() || routes.isEmpty()) {
        log.error("❌ 'routes' 정보가 없습니다: {}", body);
        throw new IllegalStateException("경로 정보가 존재하지 않습니다.");
      }

      JsonNode summary = routes.get(0).path("summary");
      if (!summary.has("distance") || !summary.has("duration")) {
        log.warn("❌ 요약 정보 누락: {}", summary);
        throw new IllegalStateException("경로 요약 정보가 누락되었습니다.");
      }

      int distance = summary.get("distance").asInt();
      int duration = summary.get("duration").asInt();


      List<Mark> marks = new ArrayList<>();

      // 출발지
      marks.add(new KakaoRouteResponse.Mark(
          KakaoRouteResponse.PointType.START,
          summary.path("origin").path("x").asDouble(),
          summary.path("origin").path("y").asDouble()
      ));

      // 도착지
      marks.add(new KakaoRouteResponse.Mark(
          KakaoRouteResponse.PointType.END,
          summary.path("destination").path("x").asDouble(),
          summary.path("destination").path("y").asDouble()
      ));

      List<RoutePoint> routePoints = new ArrayList<>();

      // 경로(vertexes)
      JsonNode sections = routes.get(0).path("sections");
      for (JsonNode section : sections) {
        for (JsonNode road : section.path("roads")) {
          JsonNode vertexes = road.path("vertexes");
          for (int i = 0; i < vertexes.size(); i += 2) {
            double x = vertexes.get(i).asDouble();
            double y = vertexes.get(i + 1).asDouble();
            routePoints.add(new KakaoRouteResponse.RoutePoint(x, y));
          }
        }
      }

      log.info("✅ 거리: {}m, 소요 시간: {}초, 포인트 개수: {}", distance, duration, routePoints.size());
      return new KakaoRouteResponse(distance, duration, marks, routePoints);

    } catch (Exception e) {
      log.error("❌ 카카오 경로 API 호출 실패: {}", e.getMessage(), e);
      throw new IllegalStateException("카카오 경로 정보를 가져오는 데 실패했습니다.");
    }
  }

  public KakaoRouteResponse getRouteFromEstimate(Estimate estimate) {
    // 1. 주소 객체 꺼내기
    Address from = estimate.getFromAddress();
    Address to = estimate.getToAddress();

    // 2. 문자열 좌표 → double 파싱
    double fromX = Double.parseDouble(from.getEntX());
    double fromY = Double.parseDouble(from.getEntY());
    double toX = Double.parseDouble(to.getEntX());
    double toY = Double.parseDouble(to.getEntY());

    // 3. 변환: TM → WGS84
    double[] fromLatLng = CoordinateConverter.convertToWGS84(fromX, fromY);
    double[] toLatLng = CoordinateConverter.convertToWGS84(toX, toY);

    // 4. "경도,위도" 문자열 조립
    String origin = toCoordString(fromLatLng);      // e.g., "127.123456,37.123456"
    String destination = toCoordString(toLatLng);

    // 5. 요청 DTO 생성 후 서비스 호출
    KakaoRouteRequest request = new KakaoRouteRequest(origin, destination);
    return getRoute(request); // 기존 메서드 호출
  }

  private String toCoordString(double[] latLng) {
    return latLng[1] + "," + latLng[0]; // lng,lat
  }

}
