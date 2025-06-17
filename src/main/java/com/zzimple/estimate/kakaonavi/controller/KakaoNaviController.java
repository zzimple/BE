package com.zzimple.estimate.kakaonavi.controller;

import com.zzimple.estimate.guest.entity.Estimate;
import com.zzimple.estimate.kakaonavi.dto.request.KakaoRouteRequest;
import com.zzimple.estimate.kakaonavi.dto.response.KakaoRouteResponse;
import com.zzimple.estimate.kakaonavi.service.KakaoNaviService;
import com.zzimple.estimate.owner.repository.EstimateRepository;
import com.zzimple.global.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao-navi")
public class KakaoNaviController {

  private final KakaoNaviService kakaoNaviService;
  private final EstimateRepository estimateRepository;

  @GetMapping("/route/{estimateNo}")
  public ResponseEntity<BaseResponse<KakaoRouteResponse>> getRouteFromEstimate(
      @PathVariable Long estimateNo
  ) {
    Estimate estimate = estimateRepository.findById(estimateNo)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 견적 번호입니다."));

    KakaoRouteResponse response = kakaoNaviService.getRouteFromEstimate(estimate);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(BaseResponse.success(response));
  }
}