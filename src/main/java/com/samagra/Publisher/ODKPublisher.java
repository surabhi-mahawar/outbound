package com.samagra.Publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ODKPublisher {
  private KafkaTemplate<String, String> simpleProducer;

  public ODKPublisher(KafkaTemplate<String, String> simpleProducer) {
    this.simpleProducer = simpleProducer;
  }

  public void send(String message) {
    System.out.println(simpleProducer.send("${odk-message}", message));
  }
}
