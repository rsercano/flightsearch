package com.tokigames.beans;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Being used to fetch flights from; https://obscure-caverns-79008.herokuapp.com/business.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BusinessFlight {

  private String uuid;
  private String flight;
  private LocalDateTime departure;
  private LocalDateTime arrival;
}
