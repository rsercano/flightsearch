package com.tokigames.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tokigames.SpringTestConfiguration;
import com.tokigames.beans.BusinessFlight;
import com.tokigames.beans.CheapFlight;
import com.tokigames.beans.SearchRequest;
import com.tokigames.beans.SearchResult;
import com.tokigames.exception.CommunicationException;
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
   * Simple search without any query & sort, should come with insertion order.
   */
  @Test
  public void searchTest_1() {
    // arrange
    List<Flight> flights = createFlights();

    // execute
    SearchResult firstPage = cut.search(SearchRequest.builder().page(0).pageSize(1).build());
    SearchResult secondPage = cut.search(SearchRequest.builder().page(1).pageSize(1).build());

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
  public void searchTest_2() {
    // arrange
    List<Flight> flights = createFlights();

    // execute
    SearchResult result = cut.search(SearchRequest.builder()
        .page(0)
        .pageSize(1)
        .sortBy(Map.of("arrivalTime", Direction.ASC))
        .query(Flight.builder().arrival("AYT").build())
        .build());

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
  public void searchTest_3() {
    // arrange
    List<Flight> flights = createFlights();

    // execute
    SearchResult result = cut.search(SearchRequest.builder()
        .page(0)
        .pageSize(5)
        .sortBy(Map.of("arrivalTime", Direction.ASC))
        .query(Flight.builder().arrival("AYT").departure("GZP").build())
        .build());

    // assert
    assertThat(result).isNotNull();
    assertThat(result.getResult().size()).isEqualTo(3);
    assertThat(result.getResult().get(0)).isEqualTo(flights.get(4));
    assertThat(result.getResult().get(1)).isEqualTo(flights.get(5));
    assertThat(result.getResult().get(2)).isEqualTo(flights.get(3));
    assertThat(result.getTotal()).isEqualTo(3);
  }

  /**
   * Search with multiple queries & multiple sort.
   */
  @Test
  public void searchTest_4() {
    // arrange
    List<Flight> flights = createFlights();

    // execute
    SearchResult result = cut.search(SearchRequest.builder()
        .page(0)
        .pageSize(5)
        .sortBy(Map.of("arrivalTime", Direction.ASC, "flightId", Direction.DESC))
        .query(Flight.builder().arrival("AYT").departure("GZP").build())
        .build());

    // assert
    assertThat(result).isNotNull();
    assertThat(result.getResult().size()).isEqualTo(3);
    assertThat(result.getResult().get(0)).isEqualTo(flights.get(4));
    assertThat(result.getResult().get(1)).isEqualTo(flights.get(5));
    assertThat(result.getResult().get(2)).isEqualTo(flights.get(3));
    assertThat(result.getTotal()).isEqualTo(3);
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

    return result;
  }
}
