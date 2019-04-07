package com.tokigames.beans;

import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import lombok.Data;
import org.springframework.data.domain.Sort.Direction;

/**
 * Request of search method, uses AND for all given parameters for {@link SearchRequest#query}.
 */
@Data
public class SearchRequest {

  private Map<String, Object> query;

  @ApiModelProperty(required = true)
  private int pageSize = 10;

  @ApiModelProperty(required = true)
  private int page = 0;

  private String sortBy;
  private Direction sortDirection;

}
