package com.tokigames.service;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.tokigames.SpringTestConfiguration;
import com.tokigames.beans.BusinessFlight;
import com.tokigames.beans.CheapFlight;
import com.tokigames.exception.CommunicationException;
import com.tokigames.model.Flight;
import com.tokigames.util.ModelConverterUtil;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringTestConfiguration.class)
public class FlightSearchHandlerTests {

  @Autowired
  MongoTemplate mongoTemplate;

  @Autowired
  HttpRequestUtil httpRequestUtil;

  @Autowired
  FlightSearchHandler cut;

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
        ZonedDateTime.parse("2019-04-06T10:20:33Z").toLocalDateTime().plus(1, ChronoUnit.HOURS));

    Mockito.when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.CHEAP_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenReturn(List.of(cheapFlight, cheapFlight2));

    Mockito.when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.BUSINESS_FLIGHTS_URL, new ParameterizedTypeReference<List<BusinessFlight>>() {
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
   * Cheap flights external services are not working
   */
  @Test(expected = CommunicationException.class)
  public void cacheFlightsTest_2() {
    // arrange
    Mockito.when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.CHEAP_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenThrow(CommunicationException.class);

    // execute
    cut.cacheFlights();
  }

  /**
   * Business flights external services are not working
   */
  @Test(expected = CommunicationException.class)
  public void cacheFlightsTest_3() {
    // arrange
    Mockito.when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.CHEAP_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenReturn(new ArrayList<>());

    Mockito.when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.BUSINESS_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenThrow(CommunicationException.class);

    // execute
    cut.cacheFlights();
  }

  /**
   * External services returns empty array
   */
  @Test(expected = CommunicationException.class)
  public void cacheFlightsTest_4() {
    // arrange
    Mockito.when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.CHEAP_FLIGHTS_URL, new ParameterizedTypeReference<List<CheapFlight>>() {
    })).thenReturn(new ArrayList<>());

    Mockito.when(httpRequestUtil.proceedGetRequest(SpringTestConfiguration.BUSINESS_FLIGHTS_URL, new ParameterizedTypeReference<List<BusinessFlight>>() {
    })).thenReturn(new ArrayList<>());

    // execute
    cut.cacheFlights();

    // assert
    assertThat(mongoTemplate.count(new Query(), Flight.class)).isEqualTo(0);
  }
}
