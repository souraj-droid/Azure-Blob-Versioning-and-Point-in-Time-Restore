package com.t258.dataprotection.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.t258.dataprotection.dto.VersionResponse;
import com.t258.dataprotection.service.VersionService;

@RestController
@RequestMapping("/api/blobs/{blobName}/versions")
public class VersionController {
  private final VersionService versions;

  public VersionController(VersionService versions) {
    this.versions = versions;
  }

  @GetMapping
  public ResponseEntity<VersionResponse> list(@PathVariable String blobName) {
    return ResponseEntity.ok(versions.listVersions(blobName));
  }

  @GetMapping("/{versionId}")
  public ResponseEntity<VersionResponse.VersionEntry> getOne(@PathVariable String blobName,
      @PathVariable String versionId) {
    VersionResponse all = versions.listVersions(blobName);
    return all.getVersions().stream()
        .filter(v -> versionId.equals(v.getVersionId()))
        .findFirst()
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new com.t258.dataprotection.exception.BlobNotFoundException(
            "Version not found: " + blobName + " version " + versionId));
  }

  @GetMapping(value = "/{versionId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> download(@PathVariable String blobName, @PathVariable String versionId) {
    byte[] data = versions.downloadVersion(blobName, versionId);
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=\"" + blobName + "\"")
        .body(data);
  }
}
