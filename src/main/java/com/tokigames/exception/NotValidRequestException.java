package com.tokigames.exception;

/**
 * Wraps validation errors, it's being catched and going out as {@link org.springframework.http.HttpStatus#UNPROCESSABLE_ENTITY}.
 *
 * <p>
 * Extends {@link RuntimeException} so it's unchecked.
 * </p>
 */
public class NotValidRequestException extends RuntimeException {

  private final String message;

  public NotValidRequestException(String message) {
    super(message);
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

}
