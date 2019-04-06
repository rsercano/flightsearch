package com.tokigames.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base MongoDB object, keeps only non-functional MongoDB fields.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Document
@Builder
@JsonDeserialize(builder = Flight.FlightBuilder.class)
public class Flight {

  private String flightId;
  private String departure;
  private String arrival;

  private LocalDateTime departureTime;
  private LocalDateTime arrivalTime;

}
