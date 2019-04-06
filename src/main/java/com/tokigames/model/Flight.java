package com.tokigames.model;

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
@EqualsAndHashCode(of = {"flightId"})
@Document
@Builder
public class Flight {

  private String flightId;
  private String departure;
  private String arrival;

  private LocalDateTime departureTime;
  private LocalDateTime arrivalTime;

}
