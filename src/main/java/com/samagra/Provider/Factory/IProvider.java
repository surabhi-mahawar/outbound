package com.samagra.Provider.Factory;

import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;
import messagerosa.core.model.XMessage;

public interface IProvider {
  void processInBoundMessage(XMessage nextMsg, XMessage currentMsg) throws Exception;

}
