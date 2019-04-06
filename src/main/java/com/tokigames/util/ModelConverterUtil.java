package com.tokigames.util;

import com.tokigames.beans.BusinessFlight;
import com.tokigames.beans.CheapFlight;
import com.tokigames.model.Flight;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Converts {@link CheapFlight} and {@link BusinessFlight} objects to {@link Flight}.
 */
public final class ModelConverterUtil {

  /**
   * Convert {@link BusinessFlight} into {@link Flight}.
   *
   * @param businessFlight {@link BusinessFlight} instance
   * @return returns {@link Flight} instance.
   */
  public static Flight convertBusinessFlightToFlight(BusinessFlight businessFlight) {
    String arrival = businessFlight.getFlight().substring(0, businessFlight.getFlight().indexOf(" "));
    String departure = businessFlight.getFlight().substring(businessFlight.getFlight().lastIndexOf(" ") + 1);

    return Flight.builder().flightId(businessFlight.getUuid()).arrival(arrival).departure(departure).arrivalTime(businessFlight.getArrival())
        .departureTime(businessFlight.getDeparture()).build();
  }

  /**
   * Convert {@link CheapFlight} into {@link Flight}.
   *
   * @param cheapFlight {@link CheapFlight} instance
   * @return returns {@link Flight} instance.
   */
  public static Flight convertCheapFlightToFlight(CheapFlight cheapFlight) {
    return Flight.builder().flightId(cheapFlight.getId()).arrival(cheapFlight.getArrival()).departure(cheapFlight.getDeparture())
        .arrivalTime(getLocalDateTimeFromMiliseconds(cheapFlight.getArrivalTime()))
        .departureTime(getLocalDateTimeFromMiliseconds(cheapFlight.getDepartureTime())).build();
  }

  private static LocalDateTime getLocalDateTimeFromMiliseconds(long value) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
  }

}
