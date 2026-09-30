package com.t258.dataprotection;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.t258.dataprotection.controller.BlobController;
import com.t258.dataprotection.controller.HealthController;
import com.t258.dataprotection.controller.RestoreController;
import com.t258.dataprotection.controller.VersionController;
import com.t258.dataprotection.dto.BlobResponse;
import com.t258.dataprotection.dto.RestoreResponse;
import com.t258.dataprotection.dto.VersionResponse;
import com.t258.dataprotection.exception.BlobNotFoundException;
import com.t258.dataprotection.service.BlobService;
import com.t258.dataprotection.service.RestoreService;
import com.t258.dataprotection.service.StorageProtectionService;
import com.t258.dataprotection.service.VersionService;

@WebMvcTest(controllers = { BlobController.class, VersionController.class, RestoreController.class, HealthController.class })
class ControllerTests {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @MockBean BlobService blobService;
  @MockBean VersionService versionService;
  @MockBean RestoreService restoreService;
  @MockBean StorageProtectionService protectionService;
  @MockBean com.azure.storage.blob.BlobServiceClient blobServiceClient;
  @MockBean com.azure.identity.DefaultAzureCredential defaultAzureCredential;

  @Test
  void healthUp() throws Exception {
    mvc.perform(get("/api/health")).andExpect(status().isOk());
  }

  @Test
  void listBlobs() throws Exception {
    when(blobService.list()).thenReturn(List.of(new BlobResponse("report.txt", "v1", 10, null, "text/plain")));
    mvc.perform(get("/api/blobs")).andExpect(status().isOk()).andExpect(jsonPath("$[0].blobName").value("report.txt"));
  }

  @Test
  void listVersions() throws Exception {
    when(versionService.listVersions("report.txt")).thenReturn(new VersionResponse("report.txt", List.of()));
    mvc.perform(get("/api/blobs/report.txt/versions")).andExpect(status().isOk());
  }

  @Test
  void invalidBlob404() throws Exception {
    when(versionService.listVersions("missing.txt")).thenThrow(new BlobNotFoundException("Blob not found: missing.txt"));
    mvc.perform(get("/api/blobs/missing.txt/versions")).andExpect(status().isNotFound());
  }

  @Test
  void invalidVersion404() throws Exception {
    when(restoreService.restoreVersion(eq("report.txt"), eq("bad")))
        .thenThrow(new BlobNotFoundException("Version not found"));
    mvc.perform(post("/api/blobs/report.txt/restore/bad")).andExpect(status().isNotFound());
  }

  @Test
  void versionRestoreOk() throws Exception {
    when(restoreService.restoreVersion(eq("report.txt"), eq("v1")))
        .thenReturn(new BlobResponse("report.txt", "v3", 5, null, "text/plain"));
    mvc.perform(post("/api/blobs/report.txt/restore/v1")).andExpect(status().isOk());
  }

  @Test
  void pitValidationBadRequestOnInvalidTime() throws Exception {
    when(restoreService.requestPointInTimeRestore(anyString())).thenThrow(new IllegalArgumentException("bad time"));
    mvc.perform(post("/api/restore/point-in-time").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(Map.of("restoreTime", "bad"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void pitRequestOk() throws Exception {
    when(restoreService.requestPointInTimeRestore(anyString()))
        .thenReturn(new RestoreResponse("pit-abc", "now", "then", "VALIDATED_PENDING_ADMIN_EXECUTION", "msg", "az ..."));
    mvc.perform(post("/api/restore/point-in-time").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(Map.of("restoreTime", "2026-09-20T14:30:00Z"))))
        .andExpect(status().isOk()).andExpect(jsonPath("$.restoreId").value("pit-abc"));
  }

  @Test
  void protectionStatus() throws Exception {
    when(protectionService.status()).thenReturn(Map.of("blobVersioning", "ENABLED"));
    mvc.perform(get("/api/protection/status")).andExpect(status().isOk());
  }
}
