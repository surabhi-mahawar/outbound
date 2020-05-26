package com.samagra.Publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BotMessageBuilderPublisher {

  private KafkaTemplate<String, String> simpleProducer;

  public BotMessageBuilderPublisher(KafkaTemplate<String, String> simpleProducer) {
    this.simpleProducer = simpleProducer;
  }

  public void send(String message) {
    log.info("bot message builder publish ");
    simpleProducer.send("${gupshup-bot-message-builder}", message);
  }
}
