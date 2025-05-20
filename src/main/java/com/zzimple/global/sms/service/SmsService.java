package com.zzimple.global.sms.service;

import com.zzimple.global.sms.config.SmsApiProperties;
import com.zzimple.global.sms.dto.request.SmsRequest;
import com.zzimple.global.sms.dto.response.SmsResponse;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.SmsErrorCode;
import com.zzimple.global.sms.service.util.SmsCertificationUtil;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

  private final WebClient smsWebClient;
  private final SmsApiProperties properties;
  private final SmsCertificationUtil smsCertificationUtil;

  private final RedisTemplate<String, String> redisTemplate;
  // Redis 저장 시간
  private static final long TTL_MINUTES = 10;

  // WebClient는 throw를 던지지 못해서 Webflux로 처리
  private static <T> Mono<T> mapError(SmsErrorCode code) {
    return Mono.error(new CustomException(code));
  }

  private SmsResponse sendSmsInternal(String to, String messageContent) {
    SmsRequest req = new SmsRequest();
    req.setMsgFrom(properties.getFromNumber());
    req.setMsgTo(to);
    req.setMessageType("1");
    req.setMessage(messageContent);
    req.setOrdDay(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    req.setOrdTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    req.setOrdNo(UUID.randomUUID().toString());

    log.info("📤 SMS 요청 파라미터: from={}, to={}, msg={}, type={}, ordNo={}",
        req.getMsgFrom(), req.getMsgTo(), req.getMessage(), req.getMessageType(), req.getOrdNo());

    SmsResponse response = smsWebClient.post()
        .uri(properties.getBaseUrl() + "/text/send")
        .header("Authorization", "Bearer " + properties.getToken())
        .header("appServiceSeq", properties.getAppServiceSeq())
        .header("apiVersion", properties.getApiVersion())
        .bodyValue(req)
        .retrieve()
        .onStatus(
            status -> status.value() == HttpStatus.BAD_REQUEST.value(),
            resp -> mapError(SmsErrorCode.INVALID_PARAMETER)
        )
        .onStatus(
            status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
            resp -> mapError(SmsErrorCode.UNAUTHORIZED)
        )
        .onStatus(
            status -> status.value() == HttpStatus.FORBIDDEN.value(),
            resp -> mapError(SmsErrorCode.FORBIDDEN)
        )
        .onStatus(
            status -> status.value() == HttpStatus.PAYLOAD_TOO_LARGE.value(),
            resp -> mapError(SmsErrorCode.MESSAGE_TOO_LONG)
        )
        .onStatus(
            HttpStatusCode::is5xxServerError,
            resp -> mapError(SmsErrorCode.EXTERNAL_SERVICE_ERROR)
        )
        .bodyToMono(SmsResponse.class) // ✅ 여기가 중요
        .onErrorMap(WebClientRequestException.class,
            ex -> new CustomException(SmsErrorCode.EXTERNAL_SERVICE_ERROR))
        .onErrorMap(Throwable.class,
            ex -> new CustomException(SmsErrorCode.UNKNOWN_ERROR))
        .block(); // ✅ 그래서 return 타입은 SmsResponse


    log.info("📩 SMS API 응답 바디: {}", response); // 🔍 로그 찍기

    // 실패했을 때 에러 처리
    if (response == null) {
      log.error("❌ SMS 응답 자체가 null입니다.");
      String key = smsCertificationUtil.buildKey(to);
      redisTemplate.delete(key);
      throw new CustomException(SmsErrorCode.SMS_SEND_FAILED);
    }

    if (response.getResCode() == null || !"0".equals(response.getResCode())) {
      log.error("❌ SMS 전송 실패 - 응답 코드: {}, 메시지: {}", response.getResCode(), response.getResMessage());
      String key = smsCertificationUtil.buildKey(to);
      redisTemplate.delete(key);
      throw new CustomException(SmsErrorCode.SMS_SEND_FAILED);
    }

    // 성공했을 때
    log.info("✅ SMS 전송 성공 - to={}, resultCode={}, message={}, trNo={}",
        response.getResData().getMsgTo(),
        response.getResCode(),
        response.getResMessage(),
        response.getResData().getTrNo());

    return response;
  }

  // 인증번호 생성 및 저장
  public String generateAndSaveCertification(String phoneNumber) {
    // 1) 인증번호 생성
    String code = smsCertificationUtil.createRandomCode();
    String key  = smsCertificationUtil.buildKey(phoneNumber);

    log.info("[SMS 인증] 인증번호 생성 - phone: {}, code: {}", phoneNumber, code);

    try {
      // 2) Redis 저장
      redisTemplate.opsForValue().set(key, code, TTL_MINUTES, TimeUnit.MINUTES);
      log.info("[SMS 인증] 인증번호 Redis 저장 성공 - key: {}, TTL: {}분", key, TTL_MINUTES);
    } catch (Exception e) {
      log.error("[SMS 인증] 인증번호 Redis 저장 실패 - key: {}, code: {}", key, code, e);
      throw new CustomException(SmsErrorCode.REDIS_SAVE_FAIL);
    }

    return code;
  }


  // 회원가입 인증 코드 보내기
  public SmsResponse sendSignupCode(String phoneNumber) {
    // 1) 인증번호 생성 및 Redis 저장
    String code = generateAndSaveCertification(phoneNumber);
    log.info("[SMS 인증] 인증번호 생성 및 Redis 저장 - phone: {}, code: {}", phoneNumber, code);

    // 2) 메시지 내용 구성
    String msg = String.format(
        "[Zzimple] 인증번호는 [%s]입니다. 해당 코드는 발송 후 3분간 유효합니다.", code
    );

    // 3) SMS 전송
    SmsResponse response = sendSmsInternal(phoneNumber, msg);

    if (response.getResData() != null) {
      log.info("[SMS 전송] 성공 - to: {}, status: {}, code: {}, message: {}",
          response.getResData().getMsgTo(),
          response.getResData().getMessageStatus(),
          response.getResCode(),
          response.getResMessage());
    } else {
      log.warn("[SMS 전송] 응답 데이터 누락 - phone: {}", phoneNumber);
    }

    return response;
  }

  // 인증 번호 검증
  public void verifySignupCode(String phoneNumber, String code) {
    log.info("[SMS 인증] 인증번호 검증 시도 - phone: {}, code: {}", phoneNumber, code);

    // 인증번호 Redis에서 꺼냄
    String key = smsCertificationUtil.buildKey(phoneNumber);
    String savedCode = redisTemplate.opsForValue().get(key);

    if (savedCode == null) {
      log.warn("[SMS 인증] 인증번호 만료 - phone: {}", phoneNumber);
      throw new CustomException(SmsErrorCode.CERTIFICATION_CODE_EXPIRED);
    }

    if (!savedCode.equals(code)) {
      log.warn("[SMS 인증] 인증번호 불일치 - phone: {}", phoneNumber);
      throw new CustomException(SmsErrorCode.INVALID_CERTIFICATION_CODE);
    }

    log.info("[SMS 인증] 인증번호 검증 성공 - phone: {}", phoneNumber);
  }

  // redis에 있는 폰 번호의 유무 검사
  public void verifyPhoneCertified(String phoneNumber) {
    String certKey = smsCertificationUtil.buildKey(phoneNumber);
    String verifiedCode = redisTemplate.opsForValue().get(certKey);

    if (verifiedCode == null) {
      log.warn("[인증 실패] 인증되지 않은 전화번호 - phone: {}", phoneNumber);
      throw new CustomException(SmsErrorCode.CERTIFICATION_CODE_EXPIRED);
    }

    log.info("[인증 성공] 전화번호 인증 완료 - phone: {}", phoneNumber);
  }

  public void removeCertifiedPhoneKey(String phoneNumber) {
    String key = smsCertificationUtil.buildKey(phoneNumber);
    Boolean deleted = redisTemplate.delete(key);

    if (Boolean.TRUE.equals(deleted)) {
      log.info("[SMS 인증] 인증 키 삭제 성공 - phone: {}", phoneNumber);
    } else {
      log.warn("[SMS 인증] 인증 키 삭제 실패 또는 존재하지 않음 - phone: {}", phoneNumber);
    }
  }
}
