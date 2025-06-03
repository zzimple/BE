package com.zzimple.owner.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "사장님 회원가입 Request")
public class OwnerSignUpRequest {

  @Schema(description = "사업자번호(아이디)", example = "1234567890")
  private String b_no;

  @Schema(description = "비밀번호")
  private String password;

  @Schema(description = "이름", example = "김짐플")
  private String userName;

  @Schema(description = "전화번호", example = "010-0000-0000")
  private String phoneNumber;

  @Schema(description = "가게 이름", example = "체리키티 이삿짐 센터")
  private String storeName;

  @Schema(description = "전체 도로명 주소", example = "서울특별시 강남구 테헤란로 223, 20층 (역삼동)")
  private String roadFullAddr;

  @Schema(description = "도로명 주소 요약", example = "테헤란로 223")
  private String roadAddrPart1;

  @Schema(description = "상세 주소", example = "20층 (역삼동)")
  private String addrDetail;

  @Schema(description = "우편번호", example = "06142")
  private String zipNo;

  @Schema(description = "이메일", example = "zzimple.official@gmail.com")
  private String email;

  @Schema(description = "보험가입 여부", example = "false")
  private Boolean insured;
}
