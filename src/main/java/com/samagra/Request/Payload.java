package com.samagra.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.samagra.Enums.PayLoadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payload {
  private String id;
  private String source;
  @JsonProperty
  private String type;
  @JsonProperty
  private InboundMessagePayload payload;
  @JsonProperty
  private Sender sender;
}
