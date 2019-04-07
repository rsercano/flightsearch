package com.tokigames.config;

import com.tokigames.beans.JsonResponseBean;
import com.tokigames.exception.CommunicationException;
import com.tokigames.exception.NotValidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Catches all exceptions thrown by whole service and convertes them into meaningful responses for controllers.
 */
@ControllerAdvice
@Slf4j
public final class GlobalExceptionHandler {

  public static final String NOT_VALID_REQUEST = "NOT_VALID_REQUEST";
  public static final String COMMUNICATION_ERROR = "COMMUNICATION_ERROR";
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

  private static final String INTERNAL_ERROR_MESSAGE = "Please check logs, there's an internal server error !";

  @ExceptionHandler(NotValidRequestException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public JsonResponseBean notValidRequestExceptionHandler(NotValidRequestException ex) {
    return new JsonResponseBean(NOT_VALID_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(CommunicationException.class)
  @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
  @ResponseBody
  public JsonResponseBean communicationException(CommunicationException ex) {
    return new JsonResponseBean(COMMUNICATION_ERROR, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public JsonResponseBean handleAllExceptions(Exception exception) {
    log.error("unexpected exception occurred", exception);
    return new JsonResponseBean(INTERNAL_ERROR, INTERNAL_ERROR_MESSAGE);
  }

}
