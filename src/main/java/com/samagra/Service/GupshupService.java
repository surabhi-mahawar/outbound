package com.samagra.Service;

import java.io.StringWriter;
import javax.validation.Valid;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.samagra.Enums.MessageType;
import com.samagra.Enums.PayLoadType;
import com.samagra.Request.MessageRequest;
import com.samagra.Request.Sender;
import com.samagra.Response.InboundMessageResponse;
import com.samagra.Response.PayloadResponse;

@Component
public class GupshupService {
  @Value("${gupshup-incoming-message}")
  private String incomingTopic;

  @Value("${gupshup-opted-out}")
  private String optedOut;

  @Autowired
  public SimpleProducer kafkaProducer;

  public void convertMessageAndPublish(@Valid MessageRequest message) {
    InboundMessageResponse response = requestToResponseObject(message);
    String kafkaMessage = jaxbObjectToXML(response);

    if (response.getType().equals(MessageType.userevent)
        && response.getPayload().getType().equals(PayLoadType.optedOut)) {
      System.out.println("user opt out send to another kafka topic");
      kafkaProducer.send(optedOut, kafkaMessage);
    } else {
      kafkaProducer.send(incomingTopic, kafkaMessage);
    }
    System.out.println(kafkaMessage);
  }



  private InboundMessageResponse requestToResponseObject(@Valid MessageRequest message) {
    InboundMessageResponse response = new InboundMessageResponse();
    response.setApp(message.getApp());

    PayloadResponse payload = new PayloadResponse();
    payload.setId(message.getPayload().getId());
    payload.setPhone(message.getPayload().getPhone());
    payload.setType(PayLoadType.of(message.getPayload().getType()));
    payload.setPayload(message.getPayload().getPayload());

    Sender sender = new Sender();
    sender.setName(message.getPayload().getSender().getName());
    sender.setPhone(message.getPayload().getSender().getPhone());

    payload.setSender(sender);
    response.setPayload(payload);

    response.setTimestamp(message.getTimestamp());
    response.setType(MessageType.of(message.getType()));
    response.setVersion(message.getVersion());
    return response;
  }



  private static String jaxbObjectToXML(InboundMessageResponse mesg) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(InboundMessageResponse.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      StringWriter sw = new StringWriter();
      jaxbMarshaller.marshal(mesg, sw);
      String xmlContent = sw.toString();
      return (xmlContent);

    } catch (JAXBException e) {
      e.printStackTrace();
    }
    return null;
  }
}
