package com.samagra.Provider.Factory;

import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;

public interface IProvider {
  void processInBoundMessage(MS3Response ms3Response, MessageResponse value) throws Exception;

}
