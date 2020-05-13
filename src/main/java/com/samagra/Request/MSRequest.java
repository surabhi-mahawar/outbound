package com.samagra.Request;

import com.samagra.Response.InboundMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MSRequest {
  private UserState userState;
  private InboundMessageResponse messageResponse;
}
