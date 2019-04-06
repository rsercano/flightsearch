package com.tokigames.controller;

import com.tokigames.service.FlightSearchHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller of {@link FlightSearchHandler}.
 */
@Controller
@RequestMapping("/flight")
@Api(value = "flight")
public class FlightController {

  private final FlightSearchHandler flightSearchHandler;

  @Autowired
  public FlightController(FlightSearchHandler flightSearchHandler) {
    this.flightSearchHandler = flightSearchHandler;
  }

  /**
   * See {@link FlightSearchHandler#cacheFlights()}.
   *
   * <p>
   * Returns {@link org.springframework.http.HttpStatus#OK} if succeeds.
   * </p>
   */
  @RequestMapping(value = "/cache", method = {RequestMethod.GET})
  @ResponseBody
  @ApiOperation(value = "refreshes cache")
  public void cacheFlights() {
    flightSearchHandler.cacheFlights();
  }
}
