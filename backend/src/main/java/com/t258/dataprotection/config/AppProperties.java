package com.t258.dataprotection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.storage")
public class AppProperties {
  /** Storage account name, e.g. t258blobxxxx. Configured via AZURE_STORAGE_ACCOUNT. */
  private String accountName = "";
  /** Blob container, default data-protection. */
  private String containerName = "data-protection";
  private String subscriptionId = "";
  private String resourceGroup = "";
  /** PIT restore window in days, default 30. */
  private int pitRestoreDays = 30;
  /** Blob soft-delete retention in days, must be > pitRestoreDays (default 31). */
  private int softDeleteDays = 31;
  /** Lifecycle: tier previous versions to Cool after N days. */
  private int tierCoolAfterDays = 7;
  /** Lifecycle: delete previous versions after N days. */
  private int deleteVersionAfterDays = 31;

  public String getAccountName() { return accountName; }
  public void setAccountName(String v) { this.accountName = v; }
  public String getContainerName() { return containerName; }
  public void setContainerName(String v) { this.containerName = v; }
  public String getSubscriptionId() { return subscriptionId; }
  public void setSubscriptionId(String v) { this.subscriptionId = v; }
  public String getResourceGroup() { return resourceGroup; }
  public void setResourceGroup(String v) { this.resourceGroup = v; }
  public int getPitRestoreDays() { return pitRestoreDays; }
  public void setPitRestoreDays(int v) { this.pitRestoreDays = v; }
  public int getSoftDeleteDays() { return softDeleteDays; }
  public void setSoftDeleteDays(int v) { this.softDeleteDays = v; }
  public int getTierCoolAfterDays() { return tierCoolAfterDays; }
  public void setTierCoolAfterDays(int v) { this.tierCoolAfterDays = v; }
  public int getDeleteVersionAfterDays() { return deleteVersionAfterDays; }
  public void setDeleteVersionAfterDays(int v) { this.deleteVersionAfterDays = v; }
}
