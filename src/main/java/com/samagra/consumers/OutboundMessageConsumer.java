package com.samagra.consumers;

import com.samagra.adapter.gs.whatsapp.GupShupWhatsappAdapter;
import com.samagra.adapter.provider.factory.ProviderFactory;
import messagerosa.core.model.XMessage;
import messagerosa.xml.XMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

@Slf4j
@Service
@RestController
public class OutboundMessageConsumer {

//  @Value("${provider.list}")
//  private String providerList;
//
//  @Autowired
//  private ProviderFactory factoryProvider;

  @Autowired
  private GupShupWhatsappAdapter gsWAdapter;

  @KafkaListener(id = "gsbmb", topics = "${inboundProcessed}")
  public void consumeMessage(String nextXmsg) throws Exception {
    log.info("inside BMBC {}", nextXmsg);

    XMessage currentXmsg = XMessageParser.parse(new ByteArrayInputStream(nextXmsg.getBytes(Charset.forName("UTF-8"))));

//TODO call to trasformer to get nextXMsg
    gsWAdapter.processInBoundMessage(currentXmsg);


//    String[] providerArray = providerList.split(",");
//    for (int i = 0; i < providerArray.length; i++) {
//      IProvider provider = factoryProvider.getProvider(providerArray[i]);
//      provider.processInBoundMessage(nextXmsg, currentXmsg);
//    }
  }
}
