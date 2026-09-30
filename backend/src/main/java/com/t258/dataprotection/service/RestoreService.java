package com.t258.dataprotection.service;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.t258.dataprotection.config.AppProperties;
import com.t258.dataprotection.dto.BlobResponse;
import com.t258.dataprotection.dto.RestoreResponse;
import com.t258.dataprotection.exception.BlobNotFoundException;
import com.t258.dataprotection.exception.RestoreException;
import com.t258.dataprotection.util.ValidationUtils;

/**
 * Two distinct recovery mechanisms:
 * 1) Version restore: copy a previous version's bytes to become the new current version.
 *    (Azure guidance: promote by copying.)
 * 2) Point-in-time restore: privileged account-level Restore Blob Ranges operation.
 *    The app VALIDATES the request and returns the exact Azure CLI command + tracking id.
 *    Actual PIT execution is done by an admin with Storage Account Contributor via
 *    {@code az storage blob restore}. This avoids faking PIT as a version copy.
 */
@Service
public class RestoreService {
  private static final Logger log = LoggerFactory.getLogger(RestoreService.class);
  private final BlobServiceClient blobServiceClient;
  private final AppProperties props;
  private final Map<String, RestoreResponse> pitRequests = new ConcurrentHashMap<>();

  public RestoreService(BlobServiceClient blobServiceClient, AppProperties props) {
    this.blobServiceClient = blobServiceClient;
    this.props = props;
  }

  /** Promote a previous version to current by copying its content. */
  public BlobResponse restoreVersion(String blobName, String versionId) {
    String name = ValidationUtils.requireValidBlobName(blobName);
    if (versionId == null || versionId.isBlank()) {
      throw new IllegalArgumentException("versionId is required");
    }
    BlobContainerClient c = blobServiceClient.getBlobContainerClient(props.getContainerName());
    var versioned = c.getBlobVersionClient(name, versionId.strip());
    if (!Boolean.TRUE.equals(versioned.exists())) {
      throw new BlobNotFoundException("Version not found: " + name + " version " + versionId);
    }
    try {
      byte[] bytes;
      try (var out = new java.io.ByteArrayOutputStream()) {
        versioned.downloadStream(out);
        bytes = out.toByteArray();
      }
      var current = c.getBlobClient(name).getBlockBlobClient();
      String contentType = null;
      try {
        contentType = versioned.getProperties().getContentType();
      } catch (Exception ignored) {}
      com.azure.storage.blob.models.BlobHttpHeaders headers = new com.azure.storage.blob.models.BlobHttpHeaders()
          .setContentType(contentType);
      try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
        current.uploadWithResponse(in, bytes.length, headers, null, null, null, null, null, null);
      }
      var p = current.getProperties();
      log.info("Restored blob {} from version {} to new version {}", name, versionId, p.getVersionId());
      return new BlobResponse(name, p.getVersionId(), p.getBlobSize(), p.getLastModified(), p.getContentType());
    } catch (Exception e) {
      throw new RestoreException("Failed to restore version: " + e.getMessage(), e);
    }
  }

  /** Validate PIT request, store it, and return CLI guidance. Does NOT fake a restore. */
  public RestoreResponse requestPointInTimeRestore(String restoreTimeInput) {
    OffsetDateTime restoreTime = ValidationUtils.requireValidRestoreTime(restoreTimeInput, props.getPitRestoreDays());
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    String restoreId = "pit-" + UUID.randomUUID().toString().substring(0, 8);
    String account = props.getAccountName() == null || props.getAccountName().isBlank() ? "<storage-account>" : props.getAccountName();
    String rg = props.getResourceGroup() == null || props.getResourceGroup().isBlank() ? "<resource-group>" : props.getResourceGroup();
    String cli = String.format(
        "az storage blob restore --account-name %s --resource-group %s --time \"%s\" --blob-range \"%s/*\"",
        account, rg, restoreTime.toString(), props.getContainerName());
    String msg = "Validated. PIT restore is a privileged admin operation (Storage Account Contributor required). "
        + "Only one restore can run per account at a time. Execute the CLI command to perform the actual Azure Restore Blob Ranges operation.";
    RestoreResponse resp = new RestoreResponse(restoreId, now.toString(), restoreTime.toString(),
        "VALIDATED_PENDING_ADMIN_EXECUTION", msg, cli);
    pitRequests.put(restoreId, resp);
    log.info("PIT restore requested id={} time={}", restoreId, restoreTime);
    return resp;
  }

  public RestoreResponse getPitStatus(String restoreId) {
    RestoreResponse r = pitRequests.get(restoreId);
    if (r == null) {
      throw new BlobNotFoundException("Restore request not found: " + restoreId);
    }
    return r;
  }
}
