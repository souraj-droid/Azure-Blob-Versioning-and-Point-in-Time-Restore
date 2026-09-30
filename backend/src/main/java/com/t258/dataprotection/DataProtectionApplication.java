package com.t258.dataprotection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.t258.dataprotection.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class DataProtectionApplication {
  public static void main(String[] args) {
    SpringApplication.run(DataProtectionApplication.class, args);
  }
}
