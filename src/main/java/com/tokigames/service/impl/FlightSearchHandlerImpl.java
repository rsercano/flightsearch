package com.tokigames.service.impl;

import com.tokigames.beans.BusinessFlight;
import com.tokigames.beans.CheapFlight;
import com.tokigames.beans.SearchRequest;
import com.tokigames.beans.SearchResult;
import com.tokigames.config.ApplicationConfig;
import com.tokigames.model.Flight;
import com.tokigames.service.FlightSearchHandler;
import com.tokigames.service.HttpRequestUtil;
import com.tokigames.util.ModelConverterUtil;
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
    log.info("trying to cache flighs from: {} and {}", applicationConfig.getCheapFlightsUrl(), applicationConfig.getBusinessFlightsUrl());

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

  @Override
  public SearchResult search(SearchRequest searchRequest) {
    //TODO
    return null;
  }

  private void insertFlights(List<CheapFlight> cheapFlightsResponse, List<BusinessFlight> businessFlights) {
    List<Flight> flights = new ArrayList<>();

    businessFlights.forEach(flight -> {
      flights.add(ModelConverterUtil.convertBusinessFlightToFlight(flight));
    });
    cheapFlightsResponse.forEach(flight -> {
      flights.add(ModelConverterUtil.convertCheapFlightToFlight(flight));
    });

    if (flights.size() > 0) {
      mongoTemplate.insert(flights, Flight.class);
      log.info("successfully inserted : {} records", flights.size());
    }
  }

}
