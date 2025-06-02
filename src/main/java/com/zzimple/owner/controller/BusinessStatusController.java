package com.zzimple.owner.controller;

import com.zzimple.global.dto.BaseResponse;
import com.zzimple.owner.exception.BusinessErrorCode;
import com.zzimple.global.exception.CustomException;
import com.zzimple.owner.dto.request.BusinessStatusRequest;
import com.zzimple.owner.dto.response.BusinessStatusCheckResponse;
import com.zzimple.owner.service.BusinessStatusService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/owner/business")
@RequiredArgsConstructor
public class BusinessStatusController {

  private final BusinessStatusService businessStatusService;
  private final RedisTemplate<String, String> redisTemplate;

  @Operation(
      summary = "[ 사장님 | 토큰 X | 사업자 번호 조회 ]",
      description =
          """
           **Parameters**  \n
           b_no: 사업자 번호  \n

           **Returns**  \n
           Y/N
           """)
  @PostMapping("/verify")
  public ResponseEntity<BaseResponse<BusinessStatusCheckResponse>> verifyBusiness(@RequestBody BusinessStatusRequest request) {

    if (request.getB_no() == null || request.getB_no().isEmpty()) {
      throw new CustomException(BusinessErrorCode.BUSINESS_NUMBER_MISSING);
    }

    BusinessStatusCheckResponse result = businessStatusService.checkBusinessStatus(request);

    // 유효한 경우만 Redis에 저장
    if (result.isValid()) {
      String redisKey = "biz:" + request.getB_no();
      redisTemplate.opsForValue()
          .set(redisKey, "Y", Duration.ofHours(3));
    }

    return ResponseEntity.ok(BaseResponse.success("사업자 상태 조회 결과", result));
  }
}