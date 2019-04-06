package com.tokigames.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Being used to fetch flights from; https://obscure-caverns-79008.herokuapp.com/cheap.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CheapFlight {

  private String id;
  private String departure;
  private String arrival;
  private long departureTime;
  private long arrivalTime;
}
