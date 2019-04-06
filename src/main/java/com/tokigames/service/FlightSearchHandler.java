package com.tokigames.service;

import com.tokigames.beans.SearchRequest;
import com.tokigames.beans.SearchResult;

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

  /**
   * Searches cached flights for requested parameters and returns result set for requested page with the total count.
   *
   * @param searchRequest accepts a {@link SearchRequest} which contains query, a sort field and pagination options.
   * @return returns {@link SearchResult} which contains result set and the total count.
   */
  SearchResult search(SearchRequest searchRequest);


}
