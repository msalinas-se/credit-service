package com.nttdata.bank.credit.exception;

/** Raised when a requested resource does not exist. */
public class ResourceNotFoundException extends RuntimeException {

  /** Creates the exception with a message. */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
