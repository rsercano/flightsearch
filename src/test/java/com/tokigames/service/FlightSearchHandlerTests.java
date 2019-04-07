package com.tokigames.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tokigames.SpringTestConfiguration;
import com.tokigames.beans.BusinessFlight;
import com.tokigames.beans.CheapFlight;
import com.tokigames.beans.SearchRequest;
import com.tokigames.beans.SearchResult;
import com.tokigames.exception.CommunicationException;
import com.tokigames.exception.NotValidRequestException;
import com.tokigames.model.Flight;
import com.tokigames.util.ModelConverterUtil;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringTestConfiguration.class)
public class FlightSearchHandlerTests {

  @Autowired
  FlightSearchHandler cut;

  @Autowired
  MongoTemplate mongoTemplate;

  @Autowired
  HttpRequestUtil httpRequestUtil;

  @After
  public void destroy() {
    mongoTemplate.getDb().drop();
  }

  /**
   * External services are working and returns expected structure with data.
   */
  @Test
  public void cacheFlightsTest_1() {
    // arrange
    CheapFlight cheapFlight = new CheapFlight("1", "SGP", "AYT", Instant.now().toEpochMilli(), Instant.now().plus(10, ChronoUnit.HOURS).toEpochMilli());
    CheapFlight cheapFlight2 = new CheapFlight("2", "FRA", "AYT", Instant.now().toEpochMilli(), Instant.now().plus(3, ChronoUnit.HOURS).toEpochMilli());

    BusinessFlight businessFlight = new BusinessFlight("3", "AYT -> GZT", ZonedDateTime.parse("2019-04-06T10:20:33Z").toLocalDateTime(),
        ZonedDateTime.parse("2019-04-06T10:20:33Z").toLocalDateTime().plusHours(1));

    when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.CHEAP_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenReturn(List.of(cheapFlight, cheapFlight2));
    when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.BUSINESS_FLIGHTS_URL, new ParameterizedTypeReference<List<BusinessFlight>>() {
    })).thenReturn(List.of(businessFlight));

    // execute
    cut.cacheFlights();

    // assert
    List<Flight> cachedFlights = mongoTemplate.find(new Query(), Flight.class);

    assertThat(cachedFlights.size()).isEqualTo(3);
    assertThat(cachedFlights.stream().filter(flight -> flight.getFlightId().equalsIgnoreCase("1")).findFirst().get())
        .isEqualTo(ModelConverterUtil.convertCheapFlightToFlight(cheapFlight));
    assertThat(cachedFlights.stream().filter(flight -> flight.getFlightId().equalsIgnoreCase("2")).findFirst().get())
        .isEqualTo(ModelConverterUtil.convertCheapFlightToFlight(cheapFlight2));
    assertThat(cachedFlights.stream().filter(flight -> flight.getFlightId().equalsIgnoreCase("3")).findFirst().get())
        .isEqualTo(ModelConverterUtil.convertBusinessFlightToFlight(businessFlight));
  }

  /**
   * External services returns empty array
   */
  @Test
  public void cacheFlightsTest_2() {
    // arrange
    when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.CHEAP_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenReturn(new ArrayList<>());

    when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.BUSINESS_FLIGHTS_URL, new ParameterizedTypeReference<List<BusinessFlight>>() {
    })).thenReturn(new ArrayList<>());

    // execute
    cut.cacheFlights();

    // assert
    assertThat(mongoTemplate.count(new Query(), Flight.class)).isEqualTo(0);
  }

  /**
   * External services are not working
   */
  @Test(expected = CommunicationException.class)
  public void cacheFlightsTest_3() {
    // arrange
    when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.CHEAP_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenThrow(CommunicationException.class);

    // execute
    cut.cacheFlights();
  }

  /**
   * null request param.
   */
  @Test(expected = NotValidRequestException.class)
  public void searchTest_1() {
    // arrange

    // execute
    cut.search(null);
  }

  /**
   * invalid page size info
   */
  @Test(expected = NotValidRequestException.class)
  public void searchTest_2() {
    // arrange
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setPageSize(0);
    searchRequest.setPage(0);

    // execute
    cut.search(searchRequest);
  }

  /**
   * invalid page info
   */
  @Test(expected = NotValidRequestException.class)
  public void searchTest_3() {
    // arrange
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setPageSize(10);
    searchRequest.setPage(-1);

    // execute
    cut.search(searchRequest);
  }

  /**
   * invalid sort param
   */
  @Test(expected = NotValidRequestException.class)
  public void searchTest_4() {
    // arrange
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setPageSize(10);
    searchRequest.setPage(0);
    searchRequest.setSortBy("not_exist");
    searchRequest.setSortDirection(Direction.ASC);

    // execute
    cut.search(searchRequest);
  }

  /**
   * Simple search without any query & sort, should come with insertion order.
   */
  @Test
  public void searchTest_5() {
    // arrange
    List<Flight> flights = createFlights();

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setPageSize(1);
    searchRequest.setPage(0);

    // execute
    SearchResult firstPage = cut.search(searchRequest);

    searchRequest.setPage(1);

    SearchResult secondPage = cut.search(searchRequest);

    // assert
    assertThat(firstPage).isNotNull();
    assertThat(firstPage.getResult().size()).isEqualTo(1);
    assertThat(firstPage.getResult().get(0)).isEqualTo(flights.get(0));
    assertThat(firstPage.getTotal()).isEqualTo(6);

    assertThat(secondPage).isNotNull();
    assertThat(secondPage.getResult().size()).isEqualTo(1);
    assertThat(secondPage.getResult().get(0)).isEqualTo(flights.get(1));
    assertThat(secondPage.getTotal()).isEqualTo(6);
  }

  /**
   * Search with one field query & with single sort.
   */
  @Test
  public void searchTest_6() {
    // arrange
    List<Flight> flights = createFlights();

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setPageSize(1);
    searchRequest.setPage(0);
    searchRequest.setSortBy("arrivalTime");
    searchRequest.setSortDirection(Direction.ASC);
    searchRequest.setQuery(Map.of("arrival", "AYT"));

    // execute
    SearchResult result = cut.search(searchRequest);

    // assert
    assertThat(result).isNotNull();
    assertThat(result.getResult().size()).isEqualTo(1);
    assertThat(result.getResult().get(0)).isEqualTo(flights.get(4));
    assertThat(result.getTotal()).isEqualTo(4);
  }

  /**
   * Search with multiple queries & single sort.
   */
  @Test
  public void searchTest_7() {
    // arrange
    List<Flight> flights = createFlights();

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setPageSize(5);
    searchRequest.setPage(0);
    searchRequest.setSortBy("arrivalTime");
    searchRequest.setSortDirection(Direction.ASC);
    searchRequest.setQuery(Map.of("arrival", "AYT", "departure", "GZP"));

    // execute
    SearchResult result = cut.search(searchRequest);

    // assert
    assertThat(result).isNotNull();
    assertThat(result.getResult().size()).isEqualTo(3);
    assertThat(result.getResult().get(0)).isEqualTo(flights.get(4));
    assertThat(result.getResult().get(1)).isEqualTo(flights.get(5));
    assertThat(result.getResult().get(2)).isEqualTo(flights.get(3));
    assertThat(result.getTotal()).isEqualTo(3);
  }

  /**
   * Search with multiple queries including date.
   */
  @Test
  public void searchTest_8() {
    // arrange
    List<Flight> flights = createFlights();

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setPageSize(5);
    searchRequest.setPage(0);
    searchRequest.setSortBy("arrivalTime");
    searchRequest.setSortDirection(Direction.ASC);
    searchRequest.setQuery(Map.of("arrival", "AYT", "departure", "GZP", "arrivalTime", flights.get(5).getArrivalTime()));

    // execute
    SearchResult result = cut.search(searchRequest);

    // assert
    assertThat(result).isNotNull();
    assertThat(result.getResult().size()).isEqualTo(1);
    assertThat(result.getResult().get(0)).isEqualTo(flights.get(5));
    assertThat(result.getTotal()).isEqualTo(1);
  }

  private List<Flight> createFlights() {
    List<Flight> result = new ArrayList<>();

    Flight flight1 = Flight.builder().arrival("AYT").arrivalTime(ZonedDateTime.parse("2019-04-06T21:11:10Z").toLocalDateTime()).flightId("1").departure("GZT")
        .departureTime(ZonedDateTime.parse("2019-04-06T22:11:10Z").toLocalDateTime().plusHours(1)).build();
    Flight flight2 = Flight.builder().arrival("SGP").arrivalTime(ZonedDateTime.parse("2019-04-06T10:01:02Z").toLocalDateTime()).flightId("2").departure("AYT")
        .departureTime(ZonedDateTime.parse("2019-04-06T10:01:02Z").toLocalDateTime().plusHours(10)).build();
    Flight flight3 = Flight.builder().arrival("GZT").arrivalTime(ZonedDateTime.parse("2019-04-07T05:05:00Z").toLocalDateTime()).flightId("3").departure("SGP")
        .departureTime(ZonedDateTime.parse("2019-04-07T05:05:00Z").toLocalDateTime().plusHours(9)).build();
    Flight flight4 = Flight.builder().arrival("AYT").arrivalTime(ZonedDateTime.parse("2019-04-07T08:10:00Z").toLocalDateTime()).flightId("4").departure("GZP")
        .departureTime(ZonedDateTime.parse("2019-04-07T08:10:00Z").toLocalDateTime().plusHours(1)).build();
    Flight flight5 = Flight.builder().arrival("AYT").arrivalTime(ZonedDateTime.parse("2019-04-06T10:10:30Z").toLocalDateTime()).flightId("5").departure("GZP")
        .departureTime(ZonedDateTime.parse("2019-04-06T10:10:30Z").toLocalDateTime().plusHours(1)).build();
    Flight flight6 = Flight.builder().arrival("AYT").arrivalTime(ZonedDateTime.parse("2019-04-06T11:10:30Z").toLocalDateTime()).flightId("6").departure("GZP")
        .departureTime(ZonedDateTime.parse("2019-04-06T11:10:30Z").toLocalDateTime().plusHours(1)).build();

    mongoTemplate.save(flight1);
    mongoTemplate.save(flight2);
    mongoTemplate.save(flight3);
    mongoTemplate.save(flight4);
    mongoTemplate.save(flight5);
    mongoTemplate.save(flight6);

    result.add(flight1);
    result.add(flight2);
    result.add(flight3);
    result.add(flight4);
    result.add(flight5);
    result.add(flight6);

    return result;
  }
}
