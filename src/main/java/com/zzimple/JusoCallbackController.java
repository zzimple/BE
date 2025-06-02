package com.zzimple;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/juso")
public class JusoCallbackController {

  // ✅ GET 방식 콜백 처리
  @GetMapping("/callback")
  public void handleGet(@RequestParam Map<String, String> params, HttpServletResponse res) throws IOException {
    String roadFullAddr = params.getOrDefault("roadFullAddr", "");
    String roadAddrPart1 = params.getOrDefault("roadAddrPart1", "");
    String addrDetail = params.getOrDefault("addrDetail", "");
    String zipNo = params.getOrDefault("zipNo", "");

    System.out.println("📮 [POST 콜백] 전달받은 주소:");
    System.out.println(" - roadFullAddr = " + roadFullAddr);
    System.out.println(" - roadAddrPart1 = " + roadAddrPart1);
    System.out.println(" - addrDetail = " + addrDetail);
    System.out.println(" - zipNo = " + zipNo);

    redirectToFront(roadFullAddr, roadAddrPart1, addrDetail, zipNo, res);
  }

// ✅ POST 방식 콜백 처리 (Map 방식으로 유연하게 처리)
  @PostMapping("/callback")
  public void handlePost(@RequestParam Map<String, String> params, HttpServletResponse res) throws IOException {
    String roadFullAddr = params.getOrDefault("roadFullAddr", "");
    String roadAddrPart1 = params.getOrDefault("roadAddrPart1", "");
    String addrDetail = params.getOrDefault("addrDetail", "");
    String zipNo = params.getOrDefault("zipNo", "");

    System.out.println("📮 [POST 콜백] 전달받은 주소:");
    System.out.println(" - roadFullAddr = " + roadFullAddr);
    System.out.println(" - roadAddrPart1 = " + roadAddrPart1);
    System.out.println(" - addrDetail = " + addrDetail);
    System.out.println(" - zipNo = " + zipNo);

    redirectToFront(roadFullAddr, roadAddrPart1, addrDetail, zipNo, res);
  }


  // ✅ 공통 리다이렉트 로직
  private void redirectToFront(String fullAddr, String roadAddrPart1, String addrDetail, String zipNo, HttpServletResponse res) throws IOException {
    String safeFullAddr = fullAddr != null ? fullAddr : "";
    String safeRoadAddrPart1 = roadAddrPart1 != null ? roadAddrPart1 : "";
    String safeAddrDetail = addrDetail != null ? addrDetail : "";
    String safeZipNo = zipNo != null ? zipNo : "";

    String target = "http://localhost:3000/juso/callback"
        + "?roadFullAddr=" + URLEncoder.encode(safeFullAddr, StandardCharsets.UTF_8)
        + "&roadAddrPart1=" + URLEncoder.encode(safeRoadAddrPart1, StandardCharsets.UTF_8)
        + "&addrDetail=" + URLEncoder.encode(safeAddrDetail, StandardCharsets.UTF_8)
        + "&zipNo=" + URLEncoder.encode(safeZipNo, StandardCharsets.UTF_8);

    res.sendRedirect(target);
  }
}
