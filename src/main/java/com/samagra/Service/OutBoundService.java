package com.samagra.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.samagra.Provider.Factory.IProvider;
import com.samagra.Provider.Factory.ProviderFactory;
import com.samagra.Transformer.Publisher.BotMessageBuilderPublisher;
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;

@Service
public class OutBoundService {

  @Value("${provider.list}")
  private String providerList;

  @Autowired
  private ProviderFactory factoryProvider;

  @Autowired
  private Ms3Service ms3Service;
  
  @Autowired
  private BotMessageBuilderPublisher bmBP ;

  public void processKafkaInResponse(MessageResponse value) throws Exception {
    
    MS3Response ms3Response = ms3Service.prepareMS3RequestAndGetResponse(value);

    String[] providerArray = providerList.split(",");
    for (int i = 0; i < providerArray.length; i++) {
      IProvider provider = factoryProvider.getProvider(providerArray[i]);
      provider.processInBoundMessage(ms3Response, value);
    }
  }
}
