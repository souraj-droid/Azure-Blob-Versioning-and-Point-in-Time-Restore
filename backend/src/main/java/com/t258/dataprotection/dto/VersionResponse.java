package com.t258.dataprotection.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class VersionResponse {
  private String blobName;
  private List<VersionEntry> versions;

  public VersionResponse() {}
  public VersionResponse(String blobName, List<VersionEntry> versions) {
    this.blobName = blobName;
    this.versions = versions;
  }
  public String getBlobName() { return blobName; }
  public void setBlobName(String v) { this.blobName = v; }
  public List<VersionEntry> getVersions() { return versions; }
  public void setVersions(List<VersionEntry> v) { this.versions = v; }

  public static class VersionEntry {
    private String versionId;
    private boolean isCurrent;
    private OffsetDateTime lastModified;
    private long contentLength;

    public VersionEntry() {}
    public VersionEntry(String versionId, boolean isCurrent, OffsetDateTime lastModified, long contentLength) {
      this.versionId = versionId;
      this.isCurrent = isCurrent;
      this.lastModified = lastModified;
      this.contentLength = contentLength;
    }
    public String getVersionId() { return versionId; }
    public void setVersionId(String v) { this.versionId = v; }
    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean v) { this.isCurrent = v; }
    public OffsetDateTime getLastModified() { return lastModified; }
    public void setLastModified(OffsetDateTime v) { this.lastModified = v; }
    public long getContentLength() { return contentLength; }
    public void setContentLength(long v) { this.contentLength = v; }
  }
}
