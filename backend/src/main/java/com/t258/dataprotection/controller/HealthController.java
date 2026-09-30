package com.t258.dataprotection.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.t258.dataprotection.service.StorageProtectionService;

@RestController
@RequestMapping("/api")
public class HealthController {
  private final StorageProtectionService protection;

  public HealthController(StorageProtectionService protection) {
    this.protection = protection;
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(Map.of(
        "status", "UP",
        "project", "24CC3046-P054",
        "team", "T258"));
  }

  @GetMapping("/protection/status")
  public ResponseEntity<Map<String, Object>> protectionStatus() {
    return ResponseEntity.ok(protection.status());
  }
}
