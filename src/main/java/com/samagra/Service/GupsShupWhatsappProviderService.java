package com.samagra.Service;

import java.util.HashMap;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import messagerosa.core.model.XMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samagra.Entity.GupshupMessageEntity;
import com.samagra.Entity.GupshupStateEntity;
import com.samagra.Provider.Factory.AbstractProvider;
import com.samagra.Provider.Factory.IProvider;
import com.samagra.Repository.MessageRepository;
import com.samagra.Repository.StateRepository;
import com.samagra.Transformer.Publisher.ODKPublisher;
import com.samagra.Transformer.Publisher.WhatsAppOutBoundPublisher;
import com.samagra.common.Request.Message;
import com.samagra.notification.Response.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Qualifier("gupshupWhatsappService")
@Service
public class GupsShupWhatsappProviderService extends AbstractProvider implements IProvider {

  @Value("${provider.gupshup.whatsapp.appname}")
  private String gupshupWhatsappApp;

  @Value("{provider.gupshup.whatsapp.apikey}")
  private String gsApiKey;

  private final static String GUPSHUP_OUTBOUND = "https://api.gupshup.io/sm/api/v1/msg";

  @Autowired
  @Qualifier("rest")
  private RestTemplate restTemplate;

  @Autowired
  private StateRepository stateRepo;

  @Autowired
  private MessageRepository msgRepo;

  @Autowired
  private ODKPublisher odkPublisher;

  @Autowired
  private WhatsAppOutBoundPublisher gsWhatsAppPublisher;

  @Override
  public void processInBoundMessage(XMessage nextMsg, XMessage currentMsg)
      throws Exception {
    // factory for channels
    // db calls
    replaceUserState(nextMsg, currentMsg);
    appendNewResponse(nextMsg, currentMsg);

    log.info("ms3Response {}", new ObjectMapper().writeValueAsString(nextMsg));

    boolean isLastResponse = false;
//            TODO ms3Response.getCurrentIndex().equals("endOfForm") ? true : false;

    if (isLastResponse) {
      odkPublisher.send(new XmlMapper().writeValueAsString(nextMsg));
    } else {
      String outMsg = new XmlMapper().writeValueAsString(nextMsg);
      gsWhatsAppPublisher.send(outMsg);
    }
  }
  private void appendNewResponse(XMessage body, XMessage kafkaResponse)
      throws JsonProcessingException {
    String message = "";
    GupshupMessageEntity msgEntity =
        msgRepo.findByPhoneNo(body.getTo().getUserIdentifier());

    if (msgEntity == null) {
      msgEntity = new GupshupMessageEntity();
      message = body.getPayload().getText();
      msgEntity.setPhoneNo(body.getTo().getUserIdentifier());
    } else {
      message = msgEntity.getMessage();
    }
    msgEntity.setMessage(message + body.getPayload().getText());
    msgEntity.setLastResponse(false);
//            TODO body.getCurrentIndex() == null ? true : false);

    msgRepo.save(msgEntity);
  }

  private void replaceUserState(XMessage body, XMessage kafkaResponse)
      throws JAXBException {
    GupshupStateEntity saveEntity =
        stateRepo.findByPhoneNo(body.getTo().getUserIdentifier());
    if (saveEntity == null) {
      saveEntity = new GupshupStateEntity();
    }
    saveEntity.setPhoneNo(body.getTo().getUserIdentifier());
    saveEntity.setPreviousPath("path");
//            TODO body.getCurrentIndex());
    saveEntity.setXmlPrevious(body.getUserState());
    saveEntity.setBotFormName(null);
    stateRepo.save(saveEntity);
  }
  private HashMap<String, String> constructWhatsAppMessage(Message message) {
    HashMap<String, String> params = new HashMap<String, String>();
    if (message.getPayload().getType().equals("message")
            && message.getPayload().getPayload().getType().equals("text")) {

      params.put("type", message.getPayload().getPayload().getType());
      params.put("text", message.getPayload().getPayload().getType());
      params.put("isHSM", String.valueOf(message.getPayload().getPayload().getHsm()));
    } else if (message.getPayload().getType().equals("message")
            && message.getPayload().getPayload().getType().equals("image")) {
      params.put("type", message.getPayload().getPayload().getType());
      params.put("originalUrl", message.getPayload().getPayload().getUrl());
      params.put("previewUrl", (message.getPayload().getPayload().getUrl()));

      if (message.getPayload().getPayload().getCaption() != null) {
        params.put("caption", message.getPayload().getPayload().getCaption());
      }
    } else if (message.getPayload().getType().equals("message")
            && (message.getPayload().getPayload().getType().equals("file")
            || message.getPayload().getPayload().getType().equals("audio")
            || message.getPayload().getPayload().getType().equals("video"))) {

      params.put("type", message.getPayload().getPayload().getType());
      params.put("url", message.getPayload().getPayload().getUrl());
      if (message.getPayload().getPayload().getFileName() != null) {
        params.put("fileName", message.getPayload().getPayload().getFileName());
      }
      if (message.getPayload().getPayload().getCaption() != null) {
        params.put("caption", message.getPayload().getPayload().getCaption());
      }
    }
    return params;
  }
}
