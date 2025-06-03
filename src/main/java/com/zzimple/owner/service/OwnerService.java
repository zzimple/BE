package com.zzimple.owner.service;

import com.zzimple.owner.exception.BusinessErrorCode;
import com.zzimple.global.exception.CustomException;
import com.zzimple.global.exception.GlobalErrorCode;
import com.zzimple.global.sms.service.SmsService;
import com.zzimple.owner.dto.request.OwnerLoginIdCheckRequest;
import com.zzimple.owner.dto.request.OwnerSignUpRequest;
import com.zzimple.owner.dto.response.OwnerLoginIdCheckResponse;
import com.zzimple.owner.dto.response.OwnerSignUpResponse;
import com.zzimple.owner.entity.Owner;
import com.zzimple.owner.repository.redis.BusinessRedisRepository;
import com.zzimple.owner.repository.OwnerRepository;
import com.zzimple.owner.store.entity.Store;
import com.zzimple.owner.store.repository.StoreRepository;
import com.zzimple.user.entity.User;
import com.zzimple.user.enums.UserRole;
import com.zzimple.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerService {

  private final OwnerRepository ownerRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final BusinessRedisRepository businessRedisRepository;
  private final StoreRepository storeRepository;

  private final SmsService smsService;

  public OwnerLoginIdCheckResponse checkLoginIdDuplicate(OwnerLoginIdCheckRequest request) {
    // 로그인 아이디(이메일) 존재 여부 확인
    boolean isDuplicate = ownerRepository.findByBusinessNumber(request.getLoginId()).isPresent();

    // 응답 생성
    return OwnerLoginIdCheckResponse.builder()
        .isDuplicate(isDuplicate)
        .build();
  }

  @Transactional
  public OwnerSignUpResponse registerOwner(OwnerSignUpRequest request) {

    // 0. 휴대폰 인증 검사
    smsService.verifyPhoneCertified(request.getPhoneNumber());

    // 1. 중복 아이디
    if (ownerRepository.findByBusinessNumber(request.getB_no()).isPresent()) {
      log.warn("[회원가입 실패] 이미 존재하는 아이디 - ID: {}", request.getB_no());
      throw new CustomException(BusinessErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
    }

    // 2. 인증된 사업자 번호 Redis에서 삭제
    businessRedisRepository.deleteById(request.getB_no());
    log.info("[회원가입] 사업자번호 인증정보 삭제 완료 - b_no: {}", request.getB_no());

    try {
      // 3. 비밀번호 암호화
      String encodedPassword = passwordEncoder.encode(request.getPassword());

      // 4. User 엔티티 생성 (role=OWNER 로 고정)
      User base = User.builder()
          .loginId(request.getB_no())
          .password(encodedPassword)
          .userName(request.getUserName())
          .phoneNumber(request.getPhoneNumber())
          .email(request.getEmail())
          .role(UserRole.OWNER)
          .build();
      userRepository.save(base);

      // 5. Owner 저장
      Owner owner = Owner.builder()
          .userId(base.getId())
          .businessNumber(request.getB_no())
          .insured(request.getInsured())
          .status("계속사업자")
          .roadFullAddr(request.getRoadFullAddr())
          .roadAddrPart1(request.getRoadAddrPart1())
          .addrDetail(request.getAddrDetail())
          .zipNo(request.getZipNo())
          .build();
      ownerRepository.save(owner);

      // 6. Store 저장 - 가게 이름과 전체 도로명 주소 저장
      Store store = Store.builder()
          .ownerId(owner.getId())
          .name(request.getStoreName())
          .address(request.getRoadFullAddr())
          .build();
      storeRepository.save(store);

      log.info("[회원가입] 회원가입 및 가게 등록 성공 - 사업자번호: {}, 이름: {}, 가게: {}",
          base.getLoginId(), base.getUserName(), store.getName());

      return OwnerSignUpResponse.builder()
          .isSuccess(true)
          .build();
    } catch (Exception e) {
      log.error("[회원가입] 회원가입 처리 중 예외 발생 - ID: {}", request.getB_no(), e);
      throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}