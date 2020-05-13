package com.samagra.Request;

import com.samagra.Response.InboundMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MS3Request {
  private UserState userState;
  private InboundMessageResponse inBoundResponse;
}
