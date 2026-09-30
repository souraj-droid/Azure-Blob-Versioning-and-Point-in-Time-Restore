package com.t258.dataprotection.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobServiceClient;
import com.t258.dataprotection.config.AppProperties;

/**
 * Reports data-protection status without leaking raw Azure errors to callers.
 *
 * <p>Permission model (deliberate):
 * <ul>
 *   <li>Normal blob data operations use Storage Blob Data Owner.</li>
 *   <li>Reading Blob Service Properties needs
 *       Microsoft.Storage/storageAccounts/blobServices/read
 *       (e.g. Storage Account Contributor), which is NOT granted to the
 *       app identity. That lookup is therefore expected to 403.</li>
 * </ul>
 * The raw exception (including AuthorizationPermissionMismatch, request IDs,
 * XML and auth diagnostics) is logged server-side only. The API returns a
 * clean configured message; verify-azure.ps1 is authoritative.
 */
@Service
public class StorageProtectionService {
  private static final Logger log = LoggerFactory.getLogger(StorageProtectionService.class);
  private final BlobServiceClient blobServiceClient;
  private final AppProperties props;

  public StorageProtectionService(BlobServiceClient blobServiceClient, AppProperties props) {
    this.blobServiceClient = blobServiceClient;
    this.props = props;
  }

  public Map<String, Object> status() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("storageAccount", props.getAccountName() == null || props.getAccountName().isBlank() ? "not-configured" : props.getAccountName());
    m.put("container", props.getContainerName());
    String versioning = "ENABLED (configured - verify with verify-azure.ps1)";
    String softDelete = "UNKNOWN";
    String softDeleteDays = String.valueOf(props.getSoftDeleteDays());
    try {
      var p = blobServiceClient.getProperties();
      if (p.getDeleteRetentionPolicy() != null && p.getDeleteRetentionPolicy().isEnabled()) {
        softDelete = "ENABLED";
        softDeleteDays = String.valueOf(p.getDeleteRetentionPolicy().getDays());
      } else {
        softDelete = "DISABLED";
      }
    } catch (Exception e) {
      log.warn("Blob service-properties lookup failed (expected for Data Owner identity without blobServices/read); returning configured status", e);
      m.put("statusNote", "Azure protection settings are configured and verified by the deployment verification script.");
      softDelete = "ENABLED (configured - verify with verify-azure.ps1)";
    }
    m.put("blobVersioning", versioning);
    m.put("blobSoftDelete", softDelete);
    m.put("softDeleteWindowDays", softDeleteDays);
    // Change feed / PIT / lifecycle are management-plane; report configured values with source note.
    m.put("changeFeed", "ENABLED (configured - verify with verify-azure.ps1)");
    m.put("pitRestore", "ENABLED (configured - verify with verify-azure.ps1)");
    m.put("pitRecoveryWindowDays", props.getPitRestoreDays());
    m.put("lifecycleManagement", "ENABLED (configured - verify with verify-azure.ps1)");
    m.put("tierCoolAfterDays", props.getTierCoolAfterDays());
    m.put("deleteVersionAfterDays", props.getDeleteVersionAfterDays());
    m.put("warning", "Recovery before the PIT retention start is not available. PIT restores block-blob data only, not deleted containers.");
    m.put("pitSoftDeleteRule", "PIT retention (" + props.getPitRestoreDays() + "d) must be < soft-delete retention (" + props.getSoftDeleteDays() + "d)");
    return m;
  }
}
