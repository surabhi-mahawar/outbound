package com.samagra.Response;

import com.samagra.Request.MessageRequest;
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
public class MS3Response {
  private boolean isLastResponse;
  private String userState;   //xml string 
  private MessageRequest messageRequest;
}
