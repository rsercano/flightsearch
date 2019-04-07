package com.tokigames.service.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.tokigames.beans.BusinessFlight;
import com.tokigames.beans.CheapFlight;
import com.tokigames.beans.SearchRequest;
import com.tokigames.beans.SearchResult;
import com.tokigames.config.ApplicationConfig;
import com.tokigames.exception.NotValidRequestException;
import com.tokigames.model.Flight;
import com.tokigames.service.FlightSearchHandler;
import com.tokigames.service.HttpRequestUtil;
import com.tokigames.util.ModelConverterUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    log.info("trying to search cached flights for request: {}", searchRequest);
    validateSearchRequest(searchRequest);

    Query query = getQuery(searchRequest);

    SearchResult result = new SearchResult();
    result.setResult(mongoTemplate.find(query, Flight.class));
    result.setTotal(mongoTemplate.count(query, Flight.class));

    log.info("successfully executed search: {}", result);
    return result;
  }

  private Query getQuery(SearchRequest searchRequest) {
    Query query = new Query();
    query.with(PageRequest.of(searchRequest.getPage(), searchRequest.getPageSize()));

    if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
      for (Entry<String, Object> queryEntry : searchRequest.getQuery().entrySet()) {
        query.addCriteria(where(queryEntry.getKey()).is(queryEntry.getValue()));
      }
    }
    if (!StringUtils.isEmpty(searchRequest.getSortBy()) && searchRequest.getSortDirection() != null) {
      query.with(new Sort(searchRequest.getSortDirection(), searchRequest.getSortBy()));
    }
    return query;
  }

  private void validateSearchRequest(SearchRequest searchRequest) {
    if (searchRequest == null || searchRequest.getPage() < 0 || searchRequest.getPageSize() <= 0) {
      log.error("the search request is invalid; searchRequest != null & page >= 0 & pageSize > 0 but; {}", searchRequest);
      throw new NotValidRequestException("the search request is invalid; searchRequest != null & page > 0 & pageSize > 0 but; " + searchRequest);
    }

    List<String> validFieldNames = Arrays.stream(Flight.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    if (!StringUtils.isEmpty(searchRequest.getSortBy()) && !validFieldNames.contains(searchRequest.getSortBy())) {
      log.error("the sort field is wrong: {}", searchRequest.getSortBy());
      throw new NotValidRequestException("the sort field is wrong: {}" + searchRequest.getSortBy());
    }

    if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty() && !checkFieldNames(searchRequest.getQuery().keySet(), validFieldNames)) {
      log.error("the sort field is wrong: {}", searchRequest.getSortBy());
      throw new NotValidRequestException("the sort field is wrong: {}" + searchRequest.getSortBy());
    }
  }

  private boolean checkFieldNames(Set<String> fields, List<String> validFields) {
    for (String sortField : fields) {
      if (!validFields.contains(sortField)) {
        return false;
      }
    }

    return true;
  }

  private void insertFlights(List<CheapFlight> cheapFlightsResponse, List<BusinessFlight> businessFlights) {
    List<Flight> flights = new ArrayList<>();

    businessFlights.forEach(flight -> flights.add(ModelConverterUtil.convertBusinessFlightToFlight(flight)));
    cheapFlightsResponse.forEach(flight -> flights.add(ModelConverterUtil.convertCheapFlightToFlight(flight)));

    if (flights.size() > 0) {
      mongoTemplate.insert(flights, Flight.class);
      log.info("successfully inserted : {} records", flights.size());
    }
  }

}
