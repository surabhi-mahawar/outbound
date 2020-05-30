package com.samagra.Transformer.Publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppOutBoundPublisher {
  private KafkaTemplate<String, String> simpleProducer;
  
  @Value("${gs-whatsapp-outbound-message}")
  private String WOM;

  public WhatsAppOutBoundPublisher(KafkaTemplate<String, String> simpleProducer) {
    this.simpleProducer = simpleProducer;
  }

  public void send(String message) {
    System.out.println(simpleProducer.send(WOM, message));
  }
}
