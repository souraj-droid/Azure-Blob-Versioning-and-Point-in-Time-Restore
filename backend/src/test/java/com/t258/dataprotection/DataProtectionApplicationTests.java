package com.t258.dataprotection;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = { "azure.storage.account-name=testaccount" })
class DataProtectionApplicationTests {
  @Test
  void contextLoads() {
  }
}
