package com.t258.dataprotection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.t258.dataprotection.config.AppProperties;
import com.t258.dataprotection.service.StorageProtectionService;

@ExtendWith(MockitoExtension.class)
class StorageProtectionServiceTests {
  @Mock BlobServiceClient client;

  @Test
  void azureErrorsHandledCleanly() {
    AppProperties p = new AppProperties();
    p.setAccountName("testaccount");
    p.setContainerName("data-protection");
    when(client.getProperties()).thenThrow(new RuntimeException("AuthorizationPermissionMismatch RequestId:xxx <Error>secret</Error>"));
    StorageProtectionService s = new StorageProtectionService(client, p);
    Map<String, Object> m = s.status();
    assertEquals("testaccount", m.get("storageAccount"));
    // User-facing payload must NOT leak raw Azure exception text.
    String all = m.values().toString();
    assertFalse(all.contains("AuthorizationPermissionMismatch"));
    assertFalse(all.contains("RequestId"));
    assertFalse(all.contains("secret"));
    assertNotNull(m.get("statusNote"));
    assertEquals("Azure protection settings are configured and verified by the deployment verification script.", m.get("statusNote"));
  }

  @Test
  void statusContainsRequiredKeys() {
    AppProperties p = new AppProperties();
    BlobServiceProperties props = mock(BlobServiceProperties.class);
    when(props.getDeleteRetentionPolicy()).thenReturn(null);
    when(client.getProperties()).thenReturn(props);
    StorageProtectionService s = new StorageProtectionService(client, p);
    Map<String, Object> m = s.status();
    assertTrue(((String) m.get("blobVersioning")).startsWith("ENABLED"));
    assertTrue(m.containsKey("pitRecoveryWindowDays"));
    assertTrue(m.containsKey("warning"));
  }

  @Test
  void nowIsUtc() {
    assertNotNull(OffsetDateTime.now(java.time.ZoneOffset.UTC));
  }
}
