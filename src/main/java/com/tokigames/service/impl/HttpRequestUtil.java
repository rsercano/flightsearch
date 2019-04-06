package com.tokigames.service.impl;

import com.tokigames.exception.CommunicationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * This class is just for internal usage of {@link com.tokigames.service.FlightSearchHandler}.
 *
 * <p>
 * Handles HTTP requests.
 * </p>
 */
@Slf4j
@Component
final class HttpRequestUtil {

  private final RestTemplate restTemplate;

  @Autowired
  HttpRequestUtil(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  <T> List<T> proceedGetRequest(String url, ParameterizedTypeReference<List<T>> type) {
    try {
      ResponseEntity<List<T>> serviceResponse = restTemplate.exchange(url, HttpMethod.GET, null, type);

      // it may be a good idea not to log all result, but since there're a few data it's okay.
      log.info("received external service response: {}", serviceResponse);

      if (serviceResponse.getStatusCode() != HttpStatus.OK) {
        throw new IllegalStateException("external url response status is not 200");
      }

      return serviceResponse.getBody();
    } catch (Exception ex) {
      log.info("unexpected error occurred while caching cheap flights", ex);
      throw new CommunicationException("unexpected error occurred while caching cheap flights");
    }
  }
}
