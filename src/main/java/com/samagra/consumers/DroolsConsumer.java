package com.samagra.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.samagra.Publisher.BotMessageBuilderPublisher;
import com.samagra.notification.Response.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DroolsConsumer {

  @Autowired
  private BotMessageBuilderPublisher bMBP;

  @KafkaListener(id = "message", topics = "${gupshup-incoming-message}")
  public void consumeMessage(String message) throws Exception {
    XmlMapper xmlMapper = new XmlMapper();
    MessageResponse value = xmlMapper.readValue(message, MessageResponse.class);
    log.info("inside the BMBP topic consumer");
    // DROOLS logic goes here.
    if (value.getPayload().getPayload().getText() != null
        || value.getPayload().getPayload().getType().equals("text"))
      bMBP.send(message);
    // ms3Service.processKafkaInResponse(value);
  }

  @KafkaListener(id = "userevent", topics = "${gupshup-opted-out}")
  public void consumeOptedOutMessage(String message)
      throws JsonMappingException, JsonProcessingException {
    XmlMapper xmlMapper = new XmlMapper();
    MessageResponse value = xmlMapper.readValue("message", MessageResponse.class);
    log.info("Consumer got message: {}", value);
  }
}
