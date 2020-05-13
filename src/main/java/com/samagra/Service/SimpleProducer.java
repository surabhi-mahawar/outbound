package com.samagra.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimpleProducer {

  private KafkaTemplate<String, String> simpleProducer;

  public SimpleProducer(KafkaTemplate<String, String> simpleProducer) {
    this.simpleProducer = simpleProducer;
  }
  public void send(String topic, String message) {
    System.out.println(simpleProducer.send(topic, message));
  }
}
