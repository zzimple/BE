package com.zzimple.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "사업자등록 상태조회 응답 DTO")
@Getter
@Setter
public class BusinessStatusResponse {

  @Schema(description = "요청 사업자 수", example = "2")
  private int request_cnt;

  @Schema(description = "정상 매칭된 사업자 수", example = "2")
  private int match_cnt;

  @Schema(description = "상태 코드 (예: OK)", example = "OK")
  private String status_code;

  @Schema(description = "사업자 상세 데이터")
  private List<BusinessData> data;

  @Getter
  @Setter
  @Schema(description = "개별 사업자 상세 응답")
  public static class BusinessData {

    @Schema(description = "사업자등록번호", example = "1234567890")
    private String b_no;

    @Schema(description = "사업자 상태", example = "계속사업자")
    private String b_stt;

    @Schema(description = "사업자 상태 코드", example = "01")
    private String b_stt_cd;

    @Schema(description = "과세유형", example = "부가가치세 일반과세자")
    private String tax_type;

    @Schema(description = "과세유형 코드", example = "01")
    private String tax_type_cd;

    @Schema(description = "폐업일자 (YYYYMMDD)", example = "20240101")
    private String end_dt;

    @Schema(description = "공동사업자 여부", example = "Y")
    private String utcc_yn;

    @Schema(description = "과세유형 전환일", example = "20240101")
    private String tax_type_change_dt;

    @Schema(description = "전자세금계산서 발급일", example = "20240101")
    private String invoice_apply_dt;

    @Schema(description = "직전 과세유형명", example = "부가가치세 일반과세자")
    private String rbf_tax_type;

    @Schema(description = "직전 과세유형 코드", example = "01")
    private String rbf_tax_type_cd;
  }
}