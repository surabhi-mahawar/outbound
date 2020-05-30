package com.samagra.Transformer.Publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BotMessageBuilderPublisher {

  private KafkaTemplate<String, String> simpleProducer;
  
  
  @Value("${gupshup-bot-message-builder}")
  private String gsBMB;

  public BotMessageBuilderPublisher(KafkaTemplate<String, String> simpleProducer) {
    this.simpleProducer = simpleProducer;
  }

  public void send(String message) {
    log.info("bot message builder publish ");
    simpleProducer.send(gsBMB, message);
  }
}
