package com.samagra.Publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppOutBoundPublisher {
  private KafkaTemplate<String, String> simpleProducer;

  public WhatsAppOutBoundPublisher(KafkaTemplate<String, String> simpleProducer) {
    this.simpleProducer = simpleProducer;
  }

  public void send(String message) {
    System.out.println(simpleProducer.send("${gs-whatsapp-outbound-message}", message));
  }
}
