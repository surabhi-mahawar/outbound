package com.samagra.Factory;

import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;

public interface Provider {

  void processInBoundMessage(MS3Response ms3Response, MessageResponse value) throws Exception;

}
