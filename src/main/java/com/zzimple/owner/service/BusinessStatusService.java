package com.zzimple.owner.service;

import com.fasterxml.jackson.databind.ObjectMapper;


import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.owner.dto.request.BusinessStatusRequest;
import com.zzimple.owner.dto.response.BusinessStatusCheckResponse;
import com.zzimple.owner.dto.response.BusinessStatusResponse;
import com.zzimple.owner.entity.BusinessRedisEntity;
import com.zzimple.owner.repository.redis.BusinessRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessStatusService {

  @Value("${api.serviceKey}")
  private String serviceKey;

  private final OkHttpClient client = new OkHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final BusinessRedisRepository businessRedisRepository;

  public BusinessStatusCheckResponse checkBusinessStatus(BusinessStatusRequest requestDTO) {
    String API_URL = "https://api.odcloud.kr/api/nts-businessman/v1/status";

    String jsonRequest;
    try {
      // DTO를 JSON 문자열로 직렬화
      jsonRequest = objectMapper.writeValueAsString(requestDTO);
    } catch (IOException e) {
      log.error("[사업자 상태 조회] 요청 직렬화 실패 - 요청 내용: {}", requestDTO, e);
      throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    RequestBody body = RequestBody.create(jsonRequest, MediaType.get("application/json; charset=utf-8"));
    String fullUrl = API_URL + "?serviceKey=" + serviceKey;

    Request request = new Request.Builder()
        .url(fullUrl)
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .post(body)
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.error("사업자 인증 API 호출 실패: {}", response);
        throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
      }

      // body가 null 방지
      ResponseBody responseBodyRaw = response.body();
      if (responseBodyRaw == null) {
        throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
      }

      String responseBody = responseBodyRaw.string();
      BusinessStatusResponse result = objectMapper.readValue(responseBody, BusinessStatusResponse.class);

      // 데이터 없을 경우 N 반환
      if (result.getData() == null || result.getData().isEmpty()) {
        return BusinessStatusCheckResponse.builder()
            .isValid(false)
            .message("해당 사업자 정보를 찾을 수 없습니다.")
            .build();
      }

      for (BusinessStatusResponse.BusinessData data : result.getData()) {
        String bNo = data.getB_no();
        String status = data.getB_stt();

        // "계속사업자"가 아닌 경우 N 반환
        if (!"계속사업자".equals(status)) {
          return BusinessStatusCheckResponse.builder()
              .isValid(false)
              .message("계속사업자가 아닙니다.")
              .build();
        }

        saveToRedis(bNo, "Y");
      }
      return BusinessStatusCheckResponse.builder()
          .isValid(true)
          .message("유효한 계속사업자입니다.")
          .build();
    } catch (IOException e) {
      log.error("사업자 상태 조회 중 IOException 발생", e);
      throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      log.error("사업자 상태 조회 중 예상치 못한 예외 발생", e);
      throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // Redis에 BusinessRedisEntity로 저장
  private void saveToRedis(String bNo, String status) {
    BusinessRedisEntity entity = new BusinessRedisEntity();
    entity.setBusinessNumber(bNo);
    entity.setStatus(status);
    businessRedisRepository.save(entity); // Redis에 저장
  }
}

