package com.tokigames;

import com.tokigames.service.FlightSearchHandler;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Main application class.
 */
@SpringBootApplication
@EnableSwagger2
@Slf4j
public class FlightSearchApplication {

  public static void main(String[] args) {
    SpringApplication.run(FlightSearchApplication.class, args);
  }

  @Autowired
  private FlightSearchHandler flightSearchHandler;

  @PostConstruct
  private void init() {
    log.info("initialization cache for external two services");
    flightSearchHandler.cacheFlights();
  }

  @Bean
  RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.setReadTimeout(Duration.ofMinutes(1)).setConnectTimeout(Duration.ofSeconds(30)).build();
  }

  @Bean
  Docket swaggerSpringMvcPlugin() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        .directModelSubstitute(LocalDateTime.class, String.class)
        .directModelSubstitute(LocalDate.class, String.class)
        .select().apis(RequestHandlerSelectors.basePackage("com.tokigames"))
        .build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("Flight Search")
        .description("by R. Sercan OZDEMİR - https://github.com/rsercano/flightsearch")
        .version("1.0.0-SNAPSHOT")
        .build();
  }
}
