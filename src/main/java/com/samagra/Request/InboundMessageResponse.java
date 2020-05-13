package com.samagra.Request;

import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samagra.Enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement
@AllArgsConstructor
@NoArgsConstructor
public class InboundMessageResponse {
  private String app;
  private Long timestamp;
  private int version;
  @JsonProperty
  private MessageType type;
  @JsonProperty
  private PayloadResponse payload;
}
