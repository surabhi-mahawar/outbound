package com.samagra.consumers;

import messagerosa.core.model.XMessage;
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

  @KafkaListener(id = "gsbmb", topics = "${inboundProcessed}")
  public void consumeMessage(String xmlMsg) throws Exception {
    log.info("inside BMBC {}", xmlMsg);

    XmlMapper xmlMapper = new XmlMapper();
    XMessage currentXmsg = xmlMapper.readValue(xmlMsg, XMessage.class);


//    MS3Response ms3Response = ms3Service.prepareMS3RequestAndGetResponse(value);
    //TODO call to get nextMsg
    XMessage nextXmsg = xmlMapper.readValue(xmlMsg, XMessage.class);

    String[] providerArray = providerList.split(",");
    for (int i = 0; i < providerArray.length; i++) {
      IProvider provider = factoryProvider.getProvider(providerArray[i]);
      provider.processInBoundMessage(nextXmsg, currentXmsg);
    }
  }
}
