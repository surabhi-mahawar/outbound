package com.samagra.Transformer.Publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ODKPublisher {
  private KafkaTemplate<String, String> simpleProducer;

  public ODKPublisher(KafkaTemplate<String, String> simpleProducer) {
    this.simpleProducer = simpleProducer;
  }

  @Value("${odk-message}")
  private String ODKM;

  
  public void send(String message) {
    System.out.println(simpleProducer.send(ODKM, message));
  }
}
