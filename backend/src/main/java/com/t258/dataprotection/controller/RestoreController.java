package com.t258.dataprotection.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.t258.dataprotection.dto.BlobResponse;
import com.t258.dataprotection.dto.RestoreRequest;
import com.t258.dataprotection.dto.RestoreResponse;
import com.t258.dataprotection.service.RestoreService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class RestoreController {
  private final RestoreService restore;

  public RestoreController(RestoreService restore) {
    this.restore = restore;
  }

  /** Version restore: promote previous version to current (copy). */
  @PostMapping("/blobs/{blobName}/restore/{versionId}")
  public ResponseEntity<BlobResponse> restoreVersion(@PathVariable String blobName,
      @PathVariable String versionId) {
    return ResponseEntity.ok(restore.restoreVersion(blobName, versionId));
  }

  /**
   * PIT restore: privileged admin operation. Validates and returns CLI guidance.
   * Requires Storage Account Contributor; do not expose to all users.
   */
  @PostMapping("/restore/point-in-time")
  public ResponseEntity<RestoreResponse> pointInTime(@Valid @RequestBody RestoreRequest req) {
    return ResponseEntity.ok(restore.requestPointInTimeRestore(req.getRestoreTime()));
  }

  @GetMapping("/restore/status/{restoreId}")
  public ResponseEntity<RestoreResponse> status(@PathVariable String restoreId) {
    return ResponseEntity.ok(restore.getPitStatus(restoreId));
  }
}
