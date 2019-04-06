package com.tokigames.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Being used to fetch flights from; https://obscure-caverns-79008.herokuapp.com/cheap.
 */
@Getter
@Setter
@ToString
public class CheapFlight {

  private String id;
  private String departure;
  private String arrival;
  private long departureTime;
  private long arrivalTime;
}
