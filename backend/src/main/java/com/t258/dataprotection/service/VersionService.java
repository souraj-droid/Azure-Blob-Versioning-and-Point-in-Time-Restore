package com.t258.dataprotection.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.t258.dataprotection.config.AppProperties;
import com.t258.dataprotection.dto.VersionResponse;
import com.t258.dataprotection.exception.BlobNotFoundException;
import com.t258.dataprotection.util.ValidationUtils;

/** Version listing / download. Distinct from point-in-time restore. */
@Service
public class VersionService {
  private final BlobServiceClient blobServiceClient;
  private final AppProperties props;

  public VersionService(BlobServiceClient blobServiceClient, AppProperties props) {
    this.blobServiceClient = blobServiceClient;
    this.props = props;
  }

  private BlobContainerClient container() {
    return blobServiceClient.getBlobContainerClient(props.getContainerName());
  }

  public VersionResponse listVersions(String blobName) {
    String name = ValidationUtils.requireValidBlobName(blobName);
    BlobContainerClient c = container();
    if (!c.exists()) {
      throw new BlobNotFoundException("Container/blob not found: " + name);
    }
    ListBlobsOptions opts = new ListBlobsOptions()
        .setPrefix(name)
        .setDetails(new BlobListDetails().setRetrieveVersions(true));
    List<VersionResponse.VersionEntry> entries = new ArrayList<>();
    boolean any = false;
    for (BlobItem item : c.listBlobs(opts, null)) {
      if (!name.equals(item.getName())) {
        continue;
      }
      any = true;
      entries.add(new VersionResponse.VersionEntry(
          item.getVersionId(),
          Boolean.TRUE.equals(item.isCurrentVersion()),
          item.getProperties() != null ? item.getProperties().getLastModified() : null,
          item.getProperties() != null && item.getProperties().getContentLength() != null
              ? item.getProperties().getContentLength() : 0));
    }
    if (!any) {
      throw new BlobNotFoundException("Blob not found: " + name);
    }
    entries.sort(Comparator.comparing(VersionResponse.VersionEntry::getLastModified,
        Comparator.nullsLast(Comparator.naturalOrder())));
    return new VersionResponse(name, entries);
  }

  public byte[] downloadVersion(String blobName, String versionId) {
    String name = ValidationUtils.requireValidBlobName(blobName);
    if (versionId == null || versionId.isBlank()) {
      throw new IllegalArgumentException("versionId is required");
    }
    BlobContainerClient c = container();
    var versioned = c.getBlobVersionClient(name, versionId.strip());
    if (!Boolean.TRUE.equals(versioned.exists())) {
      throw new BlobNotFoundException("Version not found: " + name + " version " + versionId);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    versioned.downloadStream(out);
    return out.toByteArray();
  }
}
