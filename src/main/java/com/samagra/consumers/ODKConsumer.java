package com.samagra.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RestController
public class ODKConsumer {

  @KafkaListener(id = "odk-message", topics = "${odk-message}")
  public void consumeMessage(String message) throws Exception {
    log.info("im in odk message consumer");
  }

}
