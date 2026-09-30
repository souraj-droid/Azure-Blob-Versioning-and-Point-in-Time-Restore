package com.t258.dataprotection.exception;

public class RestoreException extends RuntimeException {
  public RestoreException(String message) { super(message); }
  public RestoreException(String message, Throwable cause) { super(message, cause); }
}
