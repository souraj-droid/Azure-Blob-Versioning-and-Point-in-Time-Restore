package com.t258.dataprotection.dto;

import java.time.OffsetDateTime;

public class BlobResponse {
  private String blobName;
  private String versionId;
  private long contentLength;
  private OffsetDateTime lastModified;
  private String contentType;

  public BlobResponse() {}
  public BlobResponse(String blobName, String versionId, long contentLength, OffsetDateTime lastModified, String contentType) {
    this.blobName = blobName;
    this.versionId = versionId;
    this.contentLength = contentLength;
    this.lastModified = lastModified;
    this.contentType = contentType;
  }
  public String getBlobName() { return blobName; }
  public void setBlobName(String v) { this.blobName = v; }
  public String getVersionId() { return versionId; }
  public void setVersionId(String v) { this.versionId = v; }
  public long getContentLength() { return contentLength; }
  public void setContentLength(long v) { this.contentLength = v; }
  public OffsetDateTime getLastModified() { return lastModified; }
  public void setLastModified(OffsetDateTime v) { this.lastModified = v; }
  public String getContentType() { return contentType; }
  public void setContentType(String v) { this.contentType = v; }
}
