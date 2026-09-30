package com.t258.dataprotection.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/** Shared validation helpers. All methods throw IllegalArgumentException on failure. */
public final class ValidationUtils {
  private ValidationUtils() {}

  public static String requireValidBlobName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Blob name must not be empty");
    }
    String n = name.strip();
    if (n.length() > 1024) {
      throw new IllegalArgumentException("Blob name too long (max 1024 chars)");
    }
    if (n.contains("\\") || n.contains("..") || n.startsWith("/") || n.contains("//")) {
      throw new IllegalArgumentException("Invalid blob name: avoid path traversal, backslashes and leading slashes");
    }
    for (char c : n.toCharArray()) {
      if (c < 0x20 || c == 0x7F) {
        throw new IllegalArgumentException("Invalid blob name: control characters not allowed");
      }
    }
    return n;
  }

  public static OffsetDateTime requireValidRestoreTime(String input, int pitWindowDays) {
    if (input == null || input.isBlank()) {
      throw new IllegalArgumentException("restoreTime is required (ISO-8601 UTC, e.g. 2026-09-20T14:30:00Z)");
    }
    final OffsetDateTime t;
    try {
      t = OffsetDateTime.parse(input.strip());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("restoreTime must be ISO-8601 UTC, e.g. 2026-09-20T14:30:00Z");
    }
    OffsetDateTime now = OffsetDateTime.now(t.getOffset() != null ? t.getOffset() : java.time.ZoneOffset.UTC);
    if (t.isAfter(now)) {
      throw new IllegalArgumentException("restoreTime must not be in the future");
    }
    OffsetDateTime earliest = now.minusDays(pitWindowDays);
    if (t.isBefore(earliest)) {
      throw new IllegalArgumentException(
          "restoreTime is outside the " + pitWindowDays + "-day PIT window. Earliest allowed: " + earliest);
    }
    return t;
  }
}
