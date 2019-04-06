package com.tokigames.service;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;

/**
 * This class provides a way to handle HTTP requests for {@link com.tokigames.service.FlightSearchHandler}.
 */
public interface HttpRequestUtil {

  <T> List<T> proceedGetRequest(String url, ParameterizedTypeReference<List<T>> type);

}
