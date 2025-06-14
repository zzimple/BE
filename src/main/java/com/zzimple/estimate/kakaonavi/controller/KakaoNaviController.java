package com.zzimple.estimate.kakaonavi.controller;

import com.zzimple.estimate.kakaonavi.dto.request.KakaoRouteRequest;
import com.zzimple.estimate.kakaonavi.dto.response.KakaoRouteResponse;
import com.zzimple.estimate.kakaonavi.service.KakaoNaviService;
import com.zzimple.global.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao-navi")
public class KakaoNaviController {

  private final KakaoNaviService kakaoNaviService;

  @PostMapping("/route")
  public ResponseEntity<BaseResponse<KakaoRouteResponse>> getRoute(
      @RequestBody KakaoRouteRequest request
  ) {
    KakaoRouteResponse response = kakaoNaviService.getRoute(request);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(BaseResponse.success(response));
  }
}