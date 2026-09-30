package com.t258.dataprotection.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.t258.dataprotection.config.AppProperties;
import com.t258.dataprotection.dto.BlobResponse;
import com.t258.dataprotection.exception.BlobNotFoundException;
import com.t258.dataprotection.util.ValidationUtils;

/** Normal blob data-plane operations (Blob Data Contributor is sufficient). */
@Service
public class BlobService {
  private final BlobServiceClient blobServiceClient;
  private final AppProperties props;

  public BlobService(BlobServiceClient blobServiceClient, AppProperties props) {
    this.blobServiceClient = blobServiceClient;
    this.props = props;
  }

  private BlobContainerClient container() {
    BlobContainerClient c = blobServiceClient.getBlobContainerClient(props.getContainerName());
    if (!c.exists()) {
      c.create();
    }
    return c;
  }

  public BlobResponse upload(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File must not be empty");
    }
    String name = ValidationUtils.requireValidBlobName(file.getOriginalFilename());
    BlobContainerClient c = container();
    BlockBlobClient blob = c.getBlobClient(name).getBlockBlobClient();
    try (InputStream in = file.getInputStream()) {
      blob.upload(in, file.getSize(), true);
    }
    var p = blob.getProperties();
    return new BlobResponse(name, p.getVersionId(), p.getBlobSize(),
        p.getLastModified(), p.getContentType());
  }

  public BlobResponse overwrite(String blobName, MultipartFile file) throws IOException {
    return uploadWithName(blobName, file);
  }

  public BlobResponse uploadWithName(String blobName, MultipartFile file) throws IOException {
    String name = ValidationUtils.requireValidBlobName(blobName);
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File must not be empty");
    }
    BlobContainerClient c = container();
    BlockBlobClient blob = c.getBlobClient(name).getBlockBlobClient();
    try (InputStream in = file.getInputStream()) {
      blob.upload(in, file.getSize(), true);
    }
    var p = blob.getProperties();
    return new BlobResponse(name, p.getVersionId(), p.getBlobSize(), p.getLastModified(), p.getContentType());
  }

  public List<BlobResponse> list() {
    BlobContainerClient c = container();
    List<BlobResponse> out = new ArrayList<>();
    for (BlobItem item : c.listBlobs(new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(false)), null)) {
      if (Boolean.TRUE.equals(item.isPrefix())) {
        continue;
      }
      out.add(new BlobResponse(item.getName(), item.getVersionId(), item.getProperties() != null && item.getProperties().getContentLength() != null ? item.getProperties().getContentLength() : 0,
          item.getProperties() != null ? item.getProperties().getLastModified() : null,
          item.getProperties() != null ? item.getProperties().getContentType() : null));
    }
    return out;
  }

  public byte[] downloadCurrent(String blobName) {
    String name = ValidationUtils.requireValidBlobName(blobName);
    BlobContainerClient c = container();
    var blob = c.getBlobClient(name).getBlockBlobClient();
    if (!Boolean.TRUE.equals(blob.exists())) {
      throw new BlobNotFoundException("Blob not found: " + name);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    blob.downloadStream(out);
    return out.toByteArray();
  }

  public OffsetDateTime now() {
    return OffsetDateTime.now(java.time.ZoneOffset.UTC);
  }

  public PagedIterable<BlobItem> rawList() {
    return container().listBlobs();
  }
}
