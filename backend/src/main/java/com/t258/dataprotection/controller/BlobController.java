package com.t258.dataprotection.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.t258.dataprotection.dto.BlobResponse;
import com.t258.dataprotection.service.BlobService;

@RestController
@RequestMapping("/api/blobs")
public class BlobController {
  private final BlobService blobs;

  public BlobController(BlobService blobs) {
    this.blobs = blobs;
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BlobResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
    return ResponseEntity.ok(blobs.upload(file));
  }

  @PostMapping(value = "/{blobName}/overwrite", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BlobResponse> overwrite(@PathVariable String blobName,
      @RequestParam("file") MultipartFile file) throws Exception {
    return ResponseEntity.ok(blobs.overwrite(blobName, file));
  }

  @GetMapping
  public ResponseEntity<List<BlobResponse>> list() {
    return ResponseEntity.ok(blobs.list());
  }

  @GetMapping(value = "/{blobName}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> download(@PathVariable String blobName) {
    byte[] data = blobs.downloadCurrent(blobName);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + blobName + "\"").body(data);
  }
}
