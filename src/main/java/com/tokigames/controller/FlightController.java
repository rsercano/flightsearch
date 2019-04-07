package com.tokigames.controller;

import com.tokigames.beans.SearchRequest;
import com.tokigames.beans.SearchResult;
import com.tokigames.config.GlobalExceptionHandler;
import com.tokigames.service.FlightSearchHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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
   */
  @RequestMapping(value = "/cache", method = {RequestMethod.GET})
  @ResponseBody
  @ApiOperation(value = "refreshes cache", notes = "removes existing cached flights")
  @ApiResponses(value = {@ApiResponse(code = 500, message = GlobalExceptionHandler.INTERNAL_ERROR),
      @ApiResponse(code = 417, message = GlobalExceptionHandler.COMMUNICATION_ERROR)
  })
  public void cacheFlights() {
    flightSearchHandler.cacheFlights();
  }

  /**
   * See {@link FlightSearchHandler#search(SearchRequest)}.
   */
  @RequestMapping(value = "/search", method = {RequestMethod.POST}, consumes = "application/json", produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "searches flights", notes = "pageSize > 0 & page >=0 <br/> "
      + "<b>Flight</b> model is response model's result elements. <br/>"
      + "<b>sortBy</b> should be a property belong to <b>Flight</b> model. <br/>"
      + "<b>query</b> map's <b>keys</b> should be a property belong to <b>Flight</b> model. <br/>"
      + "<b>query</b> map's <b>value type</b> should be equal to corresponding field's type belong to <b>Flight</b> model. <br/>"
  )
  @ApiResponses(value = {@ApiResponse(code = 500, message = GlobalExceptionHandler.INTERNAL_ERROR),
      @ApiResponse(code = 422, message = GlobalExceptionHandler.NOT_VALID_REQUEST),
  })
  public SearchResult search(@RequestBody SearchRequest searchRequest) {
    return flightSearchHandler.search(searchRequest);
  }
}
