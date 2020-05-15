package com.samagra.Application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.samagra.Response.InboundMessageResponse;
import com.samagra.Service.MS3Service;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RestController
@RequestMapping(value = "/parse")
public class GupshupConsumer {

  @Autowired
  private MS3Service ms3Service;
  //
  // @RequestMapping(value = "/post", method = RequestMethod.POST)
  // public void main(String arg[])
  // throws Exception {
  // String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
  // + "<messageResponse>\n" + " <app>DemoApp</app>\n" + " <payload>\n"
  // + " <id>ABEGkYaYVSEEAhAL3SLAWwHKeKrt6s3FKB0c</id>\n" + " <payload>\n"
  // + " <caption>hahah</caption>\n" + " <text>Hi</text>\n"
  // + " <url>url</url>\n" + " </payload>\n" + " <sender>\n"
  // + " <name>Smit</name>\n" + " <phone>918x98xx21x4</phone>\n"
  // + " </sender>\n" + " <type>optedOut</type>\n" + " </payload>\n"
  // + " <timestamp>1580227766370</timestamp>\n" + " <type>userevent</type>\n"
  // + " <version>2</version>\n" + "</messageResponse>\n" + "\n" + "";
  //
  // consumeMessage(message);
  // }

   @KafkaListener(id = "message", topics = "gs-incoming-message")
  public void consumeMessage(String message)
      throws Exception {

    XmlMapper xmlMapper = new XmlMapper();
    InboundMessageResponse value = xmlMapper.readValue(message, InboundMessageResponse.class);
    log.info("Consumer got message: {}", value.getPayload().getSender().getName());
    ms3Service.processKafkaInResponse(value);
  }

   @KafkaListener(id = "userevent", topics = "gs-opted-out-message")
  public void consumeOptedOutMessage(String message)
      throws JsonMappingException, JsonProcessingException {
    XmlMapper xmlMapper = new XmlMapper();
    InboundMessageResponse value = xmlMapper.readValue("message", InboundMessageResponse.class);

    log.info("Consumer got message: {}", value);
  }
}
