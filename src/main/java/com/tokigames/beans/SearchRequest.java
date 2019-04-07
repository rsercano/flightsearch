package com.tokigames.beans;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tokigames.model.Flight;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Sort.Direction;

/**
 * Request of search method, uses AND for all given parameters for {@link SearchRequest#query}.
 */
@Getter
@Setter
@ToString
@Builder
@JsonDeserialize(builder = SearchRequest.SearchRequestBuilder.class)
public class SearchRequest {

  private Flight query;

  private int pageSize;
  private int page;

  private Map<String, Direction> sortBy;

}
