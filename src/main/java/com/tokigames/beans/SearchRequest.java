package com.tokigames.beans;

import com.tokigames.model.Flight;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Sort.Direction;

@Getter
@Setter
@ToString
public class SearchRequest {

  private Flight query;

  private int pageSize = 10;
  private int page = 0;

  private Direction sortBy;
}
