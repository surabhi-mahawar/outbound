package com.samagra.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.samagra.Entity.BotEntity;
import com.samagra.Entity.GupshupMessageEntity;
import com.samagra.Entity.GupshupStateEntity;
import com.samagra.Factory.AbstractProvider;
import com.samagra.Factory.IProvider;
import com.samagra.Repository.BotRepo;
import com.samagra.Repository.MessageRepository;
import com.samagra.Repository.StateRepository;
import com.samagra.common.Request.Message;
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;

@Qualifier("gupshupWhatsappService")
@Service
public class GupsShupWhatsappProviderService extends AbstractProvider implements IProvider {

  @Value("${provider.gupshup.whatsapp.appname}")
  private String gupshupWhatsappApp;
  private final static String GUPSHUP_OUTBOUND = "https://api.gupshup.io/sm/api/v1/msg";

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private StateRepository stateRepo;

  @Autowired
  private MessageRepository msgRepo;

  @Autowired
  private BotRepo botRepo;

  @Override
  public void processInBoundMessage(MS3Response ms3Response, MessageResponse kafkaResponse,
      boolean firstMessage) throws Exception {
    if (firstMessage) {
      sendGupshupWhatsAppOutBound(ms3Response, kafkaResponse, null, firstMessage);
    } else {
      boolean isLastResponse = ms3Response.getCurrentIndex() == null ? true : false;
      if (isLastResponse) {
        // call to odk
      } else {
        if (ms3Response.getIsPreviousInputCorrect() == 0) {
          replaceUserState(ms3Response, kafkaResponse);
          appendNewResponse(ms3Response, kafkaResponse);
          sendGupshupWhatsAppOutBound(ms3Response, kafkaResponse, null, false);
        } else if (ms3Response.getIsPreviousInputCorrect() == 1) {
          sendGupshupWhatsAppOutBound(ms3Response, kafkaResponse, null, false);
        } else {
          BotEntity botEntity = botRepo.findByFormId(0);
          sendGupshupWhatsAppOutBound(ms3Response, kafkaResponse,
              botEntity.getWrongDefaultMessage(), false);
        }
      }
    }
  }

  private void sendGupshupWhatsAppOutBound(MS3Response ms3Response, MessageResponse value,
      String wrongMessage, boolean firstMessage) throws Exception {
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("channel", "whatsapp");
    params.put("source", "917834811114");
    params.put("destination", "919415787824");
    params.put("src.name", gupshupWhatsappApp);
    params.put("message", prepareMessage(ms3Response, wrongMessage, firstMessage));
    // params.put("type", "text");
    // params.put("isHSM", "false");

    String str2 =
        URLEncodedUtils.format(hashMapToNameValuePairList(params), '&', Charset.defaultCharset());

    HttpEntity<String> request = new HttpEntity<String>(str2, getVerifyHttpHeader());
    restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
    restTemplate.postForObject(GUPSHUP_OUTBOUND, request, String.class);
  }



  private String prepareMessage(MS3Response ms3Response, String wrongMessage,
      boolean firstMessage) {
    String message = "";
    if (!Strings.isNotEmpty(wrongMessage)) {
      message = wrongMessage;
    } else if (firstMessage) {
      message = "BRO ::Choose Any One of the Options :*Door 1* *Door 2* *Door 3*";
    } else {
      message = ms3Response.getNextMessage();
    }
    return message;
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



  public static List<NameValuePair> hashMapToNameValuePairList(HashMap<String, String> map) {
    List<NameValuePair> list = new ArrayList<NameValuePair>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      list.add(new BasicNameValuePair(key, value));
    }
    return list;
  }


  private static HttpHeaders getVerifyHttpHeader() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("Cache-Control", "no-cache");
    headers.add("apikey", "c2ed3ece4e7c40eac0af0e012866e090 ");
    return headers;
  }

  public static MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
        new MappingJackson2HttpMessageConverter();
    mappingJackson2HttpMessageConverter
        .setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));
    return mappingJackson2HttpMessageConverter;
  }


  private void appendNewResponse(MS3Response body, MessageResponse kafkaResponse)
      throws JsonProcessingException {
    GupshupMessageEntity msgEntity =
        msgRepo.findByPhoneNo(kafkaResponse.getPayload().getSender().getPhone());

    if (msgEntity == null) {
      msgEntity = new GupshupMessageEntity();
      msgEntity.setMessage(body.getNextMessage());
    } else {
      msgEntity.setMessage(msgEntity.getMessage() + body.getNextMessage());
    }
    msgEntity.setPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
    msgEntity.setLastResponse(body.getCurrentIndex() == null ? true : false);

    msgRepo.save(msgEntity);
  }

  private void replaceUserState(MS3Response body, MessageResponse kafkaResponse)
      throws JAXBException {
    GupshupStateEntity saveEntity =
        stateRepo.findByPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
    if (saveEntity == null) {
      saveEntity = new GupshupStateEntity();
    }
    saveEntity.setPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
    saveEntity.setPreviousPath(body.getCurrentIndex());
    saveEntity.setXmlPrevious(body.getCurrentResponseState());

    if (Integer.valueOf(kafkaResponse.getPayload().getPayload().getText()) > 0) {
      saveEntity.setFormId(Integer.valueOf(kafkaResponse.getPayload().getPayload().getText()));
    }

    stateRepo.save(saveEntity);
  }
}
