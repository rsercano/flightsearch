package com.tokigames.service;

/**
 * Main handler to search flights & re-cache flights.
 */
public interface FlightSearchHandler {

  /**
   * Fills flights by caching from two external sources.
   *
   * <p>
   * Removes all existing flights.
   * </p>
   */
  void cacheFlights();

}
