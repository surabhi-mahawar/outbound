package com.samagra.Request;

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
