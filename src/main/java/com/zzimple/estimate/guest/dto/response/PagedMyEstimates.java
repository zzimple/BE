package com.zzimple.estimate.guest.dto.response;

import com.zzimple.estimate.guest.dto.MyEstimatePreview;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PagedMyEstimates {

  private List<MyEstimatePreview> estimates;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
}
