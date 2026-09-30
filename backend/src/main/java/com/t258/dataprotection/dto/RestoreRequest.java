package com.t258.dataprotection.dto;

import jakarta.validation.constraints.NotBlank;

public class RestoreRequest {
  @NotBlank(message = "restoreTime is required, ISO-8601 UTC e.g. 2026-09-20T14:30:00Z")
  private String restoreTime;

  public RestoreRequest() {}
  public RestoreRequest(String restoreTime) { this.restoreTime = restoreTime; }
  public String getRestoreTime() { return restoreTime; }
  public void setRestoreTime(String v) { this.restoreTime = v; }
}
