package com.tokigames.beans;

import com.tokigames.model.Flight;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Wraps {@link Flight} model for pageable search requests.
 */
@Getter
@Setter
@ToString
public class SearchResult {

  private long total;
  private List<Flight> result = new ArrayList<>();

}
