package com.samagra.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.samagra.Provider.Factory.IProvider;
import com.samagra.Provider.Factory.ProviderFactory;
import com.samagra.Service.Ms3Service;
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RestController
public class BotMessageBuilderConsumer {

  @Value("${provider.list}")
  private String providerList;

  @Autowired
  private ProviderFactory factoryProvider;

  @Autowired
  private Ms3Service ms3Service;


  @KafkaListener(id = "gsbmb", topics = "${gupshup-bot-message-builder}")
  public void consumeMessage(String message) throws Exception {
    log.info("inside BMBC {}", message);
    XmlMapper xmlMapper = new XmlMapper();

    MessageResponse value = xmlMapper.readValue(message, MessageResponse.class);
    
    MS3Response ms3Response = ms3Service.prepareMS3RequestAndGetResponse(value);

    String[] providerArray = providerList.split(",");
    for (int i = 0; i < providerArray.length; i++) {
      IProvider provider = factoryProvider.getProvider(providerArray[i]);
      provider.processInBoundMessage(ms3Response, value);
    }
  }
}
