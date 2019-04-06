package com.tokigames.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Default bean for controller error responses, see {@link com.tokigames.config.GlobalExceptionHandler}.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JsonResponseBean {

  private String responseCode;
  private String responseMessage;

}
