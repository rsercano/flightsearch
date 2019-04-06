package com.tokigames.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keeps the application configuration read from resources/application.properties.
 */
@Component
@Getter
@Setter
@ToString
public class ApplicationConfig {

  private String cheapFlightsUrl;
  private String businessFlightsUrl;

  public ApplicationConfig(@Value("${cheapflights.url}") String cheapFlightsUrl, @Value("${businessflights.url}") String businessFlightsUrl) {
    this.cheapFlightsUrl = cheapFlightsUrl;
    this.businessFlightsUrl = businessFlightsUrl;
  }
}
