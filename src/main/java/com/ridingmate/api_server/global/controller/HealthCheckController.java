package com.ridingmate.api_server.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ALB 헬스 체크용 컨트롤러
 * 루트 경로(/)에 대한 요청을 처리하여 NoResourceFoundException 방지
 */
@RestController
public class HealthCheckController {

    @GetMapping("/")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}

