package com.t258.dataprotection.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

/**
 * Creates Azure Blob clients using DefaultAzureCredential (passwordless).
 * Local dev: {@code az login}. In App Service: system-assigned managed identity.
 * No storage keys are used anywhere.
 */
@Configuration
public class AzureBlobConfig {
  private static final Logger log = LoggerFactory.getLogger(AzureBlobConfig.class);
  private final AppProperties props;

  public AzureBlobConfig(AppProperties props) {
    this.props = props;
  }

  @Bean
  public DefaultAzureCredential defaultAzureCredential() {
    return new DefaultAzureCredentialBuilder().build();
  }

  @Bean
  @ConditionalOnProperty(prefix = "azure.storage", name = "account-name")
  public BlobServiceClient blobServiceClient(DefaultAzureCredential credential) {
    String account = props.getAccountName();
    if (account == null || account.isBlank()) {
      log.warn("AZURE_STORAGE_ACCOUNT not set - BlobServiceClient will fail on use. Set env var to enable Azure calls.");
    }
    String endpoint = String.format("https://%s.blob.core.windows.net", account == null || account.isBlank() ? "not-configured" : account);
    log.info("Building BlobServiceClient for endpoint {}", endpoint);
    return new BlobServiceClientBuilder()
        .endpoint(endpoint)
        .credential(credential)
        .buildClient();
  }
}
