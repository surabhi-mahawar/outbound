package com.samagra.Service;

import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OutboundMessage {
  private MS3Response ms3Response;
  private MessageResponse messageResponse;
}
