package com.samagra.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.samagra.Factory.IProvider;
import com.samagra.Factory.ProviderFactory;
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;

@Service
public class OutBoundService {

  @Value("${provider.list}")
  private String providerList;
  
  
  @Autowired
  private Ms3Service ms3Service;

  public void processKafkaInResponse(MessageResponse value) throws Exception {
    IProvider provider = null;
    MS3Response ms3Response = ms3Service.prepareMS3RequestAndGetResponse(value);

    String[] providerArray = providerList.split(",");
    for (int i = 0; i < providerArray.length; i++) {
      provider = ProviderFactory.getProvider(providerArray[i]);
      provider.processInBoundMessage(ms3Response, value);
    }
  }
}
