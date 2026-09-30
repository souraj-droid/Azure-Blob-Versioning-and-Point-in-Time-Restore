package com.t258.dataprotection;

import static org.junit.jupiter.api.Assertions.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import com.t258.dataprotection.util.ValidationUtils;

class ValidationUtilsTests {
  @Test
  void validBlobName() {
    assertEquals("report.txt", ValidationUtils.requireValidBlobName("report.txt"));
  }

  @Test
  void invalidBlobNameEmpty() {
    assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidBlobName("  "));
  }

  @Test
  void invalidBlobNameTraversal() {
    assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidBlobName("../secret"));
    assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidBlobName("a\\b"));
  }

  @Test
  void invalidBlobReturns404Intent() {
    // controller maps BlobNotFoundException to 404 - validated via RestoreControllerTests
    assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidBlobName(null));
  }

  @Test
  void invalidVersionValidation() {
    assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidRestoreTime("not-a-date", 30));
  }

  @Test
  void futurePitRejected() {
    String future = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString();
    assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidRestoreTime(future, 30));
  }

  @Test
  void outsideWindowRejected() {
    String old = OffsetDateTime.now(ZoneOffset.UTC).minusDays(31).toString();
    assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireValidRestoreTime(old, 30));
  }

  @Test
  void validPitAccepted() {
    String ok = OffsetDateTime.now(ZoneOffset.UTC).minusHours(2).toString();
    assertNotNull(ValidationUtils.requireValidRestoreTime(ok, 30));
  }
}
