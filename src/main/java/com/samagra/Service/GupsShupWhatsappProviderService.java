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
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Qualifier("gupshupWhatsappService")
@Service
public class GupsShupWhatsappProviderService extends AbstractProvider implements IProvider {

  @Value("${provider.gupshup.whatsapp.appname}")
  private String gupshupWhatsappApp;
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
  private WhatsAppOutBoundPublisher WOBP;

  @Override
  public void processInBoundMessage(MS3Response ms3Response, MessageResponse kafkaResponse)
      throws Exception {

    // factory for channels

    // db calls
    replaceUserState(ms3Response, kafkaResponse);
    appendNewResponse(ms3Response, kafkaResponse);

    log.info("ms3Response {}", new ObjectMapper().writeValueAsString(ms3Response));

    boolean isLastResponse = ms3Response.getCurrentIndex().equals("endOfForm") ? true : false;

    if (isLastResponse) {
      odkPublisher.send(new ObjectMapper().writeValueAsString(ms3Response));
    } else {
      OutboundMessage kafkaOutboundMessage = new OutboundMessage();
      kafkaOutboundMessage.setMessageResponse(kafkaResponse);
      kafkaOutboundMessage.setMs3Response(ms3Response);
      // prepare send message using ms3response and kafkaresponse you need both of them
      // String outMsg = new ObjectMapper().writeValueAsString(kafkaOutboundMessage);

      // WOBP.send(outMsg);
      sendGupshupWhatsAppOutBound(ms3Response, kafkaResponse);
    }
  }

  private void sendGupshupWhatsAppOutBound(MS3Response ms3Response, MessageResponse value)
      throws Exception {
    String message = ms3Response.getNextMessage();

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("channel", "whatsapp");
    params.put("source", "917834811114");
    params.put("destination", "919718908699");
    params.put("src.name", gupshupWhatsappApp);

    // params.put("type", "text");
    params.put("message", message);
    // params.put("isHSM", "false");

    String str2 =
        URLEncodedUtils.format(hashMapToNameValuePairList(params), '&', Charset.defaultCharset());

    System.out.println("Question for user: " + message);
    HttpEntity<String> request = new HttpEntity<String>(str2, getVerifyHttpHeader());
    restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
    restTemplate.postForObject(GUPSHUP_OUTBOUND, request, String.class);
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
    headers.add("apikey", "8cfab6a264784290c2b736f2f53b51b4");
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
    String message = "";
    GupshupMessageEntity msgEntity =
        msgRepo.findByPhoneNo(kafkaResponse.getPayload().getSender().getPhone());


    if (msgEntity == null) {
      msgEntity = new GupshupMessageEntity();
      message = body.getNextMessage();
      msgEntity.setPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
    } else {
      message = msgEntity.getMessage();
    }
    msgEntity.setMessage(message + body.getNextMessage());
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
    saveEntity.setBotFormName(null);
    stateRepo.save(saveEntity);
  }

}
