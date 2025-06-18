package com.zzimple.estimate.gpt.controller;

import com.zzimple.estimate.gpt.dto.request.ChatGptRequest;
import com.zzimple.estimate.gpt.dto.response.ChatGptResponse;
import com.zzimple.estimate.gpt.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gpt/compare")
@RequiredArgsConstructor
public class ChatGptController {

  private final ChatGptService estimateCompareService;

  @PostMapping
  public ResponseEntity<ChatGptResponse> compareEstimates(@RequestBody ChatGptRequest request) {
    ChatGptResponse response = estimateCompareService.getChatGptResponse(request);
    return ResponseEntity.ok(response);
  }
}