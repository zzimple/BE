package com.zzimple.estimate.guest.dto;

import com.zzimple.estimate.guest.dto.response.EstimateResponsePreview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EstimateResponseList {
  private Long estimateNo; // 고객이 보낸 견적서 번호
  private List<EstimateResponsePreview> responses; // 사장님들의 응답 리스트
}
