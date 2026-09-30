package com.t258.dataprotection.dto;

public class RestoreResponse {
  private String restoreId;
  private String requestedTime;
  private String restoreTime;
  private String status;
  private String message;
  private String cliCommand;

  public RestoreResponse() {}
  public RestoreResponse(String restoreId, String requestedTime, String restoreTime, String status, String message, String cliCommand) {
    this.restoreId = restoreId;
    this.requestedTime = requestedTime;
    this.restoreTime = restoreTime;
    this.status = status;
    this.message = message;
    this.cliCommand = cliCommand;
  }
  public String getRestoreId() { return restoreId; }
  public void setRestoreId(String v) { this.restoreId = v; }
  public String getRequestedTime() { return requestedTime; }
  public void setRequestedTime(String v) { this.requestedTime = v; }
  public String getRestoreTime() { return restoreTime; }
  public void setRestoreTime(String v) { this.restoreTime = v; }
  public String getStatus() { return status; }
  public void setStatus(String v) { this.status = v; }
  public String getMessage() { return message; }
  public void setMessage(String v) { this.message = v; }
  public String getCliCommand() { return cliCommand; }
  public void setCliCommand(String v) { this.cliCommand = v; }
}
