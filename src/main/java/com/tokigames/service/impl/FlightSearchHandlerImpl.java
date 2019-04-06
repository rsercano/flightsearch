package com.tokigames.service.impl;

import com.tokigames.beans.BusinessFlight;
import com.tokigames.beans.CheapFlight;
import com.tokigames.config.ApplicationConfig;
import com.tokigames.model.Flight;
import com.tokigames.service.FlightSearchHandler;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class FlightSearchHandlerImpl implements FlightSearchHandler {

  private final MongoTemplate mongoTemplate;
  private final ApplicationConfig applicationConfig;
  private final HttpRequestUtil httpRequestUtil;

  @Autowired
  public FlightSearchHandlerImpl(MongoTemplate mongoTemplate, ApplicationConfig applicationConfig, HttpRequestUtil httpRequestUtil) {
    this.mongoTemplate = mongoTemplate;
    this.applicationConfig = applicationConfig;
    this.httpRequestUtil = httpRequestUtil;
  }

  @Override
  public void cacheFlights() {
    log.info("trying to cache flights");

    // clear cache
    mongoTemplate.remove(new Query(), Flight.class);

    // insert new ones.
    List<CheapFlight> cheapFlightsResponse = httpRequestUtil.proceedGetRequest(applicationConfig.getCheapFlightsUrl(), new ParameterizedTypeReference<List<CheapFlight>>() {
    });
    List<BusinessFlight> businessFlights = httpRequestUtil.proceedGetRequest(applicationConfig.getBusinessFlightsUrl(), new ParameterizedTypeReference<List<BusinessFlight>>() {
    });
    insertFlights(cheapFlightsResponse, businessFlights);

    log.info("successfully cached both services");
  }

  private void insertFlights(List<CheapFlight> cheapFlightsResponse, List<BusinessFlight> businessFlights) {

    List<Flight> flights = new ArrayList<>(getFlightsFromCheapFlights(cheapFlightsResponse));
    flights.addAll(getFlightsFromBusinessFlights(businessFlights));

    if (flights.size() > 0) {
      mongoTemplate.insert(flights, Flight.class);
      log.info("successfully inserted : {} records", flights.size());
    }
  }

  private List<Flight> getFlightsFromBusinessFlights(List<BusinessFlight> businessFlights) {
    List<Flight> result = new ArrayList<>();

    businessFlights.forEach(f -> {
      String arrival = f.getFlight().substring(0, f.getFlight().indexOf(" "));
      String departure = f.getFlight().substring(f.getFlight().lastIndexOf(" ") + 1);

      Flight flight = Flight.builder().flightId(f.getUuid()).arrival(arrival).departure(departure).arrivalTime(f.getArrival())
          .departureTime(f.getDeparture()).build();

      result.add(flight);
    });

    return result;
  }

  private List<Flight> getFlightsFromCheapFlights(List<CheapFlight> cheapFlightsResponse) {
    List<Flight> result = new ArrayList<>();

    cheapFlightsResponse.forEach(f -> {
      Flight flight = Flight.builder().flightId(f.getId()).arrival(f.getArrival()).departure(f.getDeparture()).arrivalTime(getLocalDateTimeFromMiliseconds(f.getArrivalTime()))
          .departureTime(getLocalDateTimeFromMiliseconds(f.getDepartureTime())).build();

      result.add(flight);
    });

    return result;
  }

  private LocalDateTime getLocalDateTimeFromMiliseconds(long value) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
  }

}
