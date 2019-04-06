package com.tokigames.exception;

/**
 * Wraps communication errors, it's being catched and going out as {@link org.springframework.http.HttpStatus#EXPECTATION_FAILED}.
 *
 * <p>
 * Extends {@link RuntimeException} so it's unchecked.
 * </p>
 */
public class CommunicationException extends RuntimeException {

  private final String message;

  public CommunicationException(String message) {
    super(message);
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

}
