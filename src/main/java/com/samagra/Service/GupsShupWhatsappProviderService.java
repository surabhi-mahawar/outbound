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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samagra.Entity.GupshupMessageEntity;
import com.samagra.Entity.GupshupStateEntity;
import com.samagra.Factory.IProvider;
import com.samagra.Factory.AbstractProvider;
import com.samagra.Repository.MessageRepository;
import com.samagra.Repository.StateRepository;
import com.samagra.common.Request.Message;
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;

public class GupsShupWhatsappProviderService extends AbstractProvider implements IProvider {

  @Value("${provider.gupshup.whatsapp.appname}")
  private String gupshupWhatsappApp;
  private final static String GUPSHUP_OUTBOUND = "https://api.gupshup.io/sm/api/v1/msg";

  @Autowired
  private static RestTemplate restTemplate;

  @Autowired
  private StateRepository stateRepo;

  @Autowired
  private MessageRepository msgRepo;

  @Override
  public void processInBoundMessage(MS3Response ms3Response, MessageResponse kafkaResponse)
      throws Exception {

    // db calls
    replaceUserState(ms3Response, kafkaResponse);
    appendNewResponse(ms3Response, kafkaResponse);

    boolean isLastResponse = ms3Response.getCurrentIndex() == null ? true : false;

    if (isLastResponse) {
      // call to odk
    } else {
      sendGupshupWhatsAppOutBound(ms3Response, kafkaResponse);
    }
  }



  private void sendGupshupWhatsAppOutBound(MS3Response ms3Response, MessageResponse value)
      throws Exception {
    String message = ms3Response.getNextMessage();

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("channel", "whatsapp");
    params.put("source", value.getPayload().getSource());
    params.put("destination", value.getPayload().getPhone());
    params.put("src", gupshupWhatsappApp);

    params.put("type", "text");
    params.put("text", message);
    params.put("isHSM", "false");

    String str2 =
        URLEncodedUtils.format(hashMapToNameValuePairList(params), '&', Charset.defaultCharset());

    HttpEntity<String> request = new HttpEntity<String>(str2, getVerifyHttpHeader());
    restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
    restTemplate.postForObject(GUPSHUP_OUTBOUND, request, String.class);
  }



  private HashMap<String, String> constructWhatsAppMessage(Message message) {
    HashMap<String, String> params = new HashMap<String, String>();
    if (message.getPayload().getType().equals("message")
        && message.getPayload().getMsgPayload().getType().equals("text")) {

      params.put("type", message.getPayload().getMsgPayload().getType());
      params.put("text", message.getPayload().getMsgPayload().getType());
      params.put("isHSM", String.valueOf(message.getPayload().getMsgPayload().isHSM()));
    } else if (message.getPayload().getType().equals("message")
        && message.getPayload().getMsgPayload().getType().equals("image")) {
      params.put("type", message.getPayload().getMsgPayload().getType());
      params.put("originalUrl", message.getPayload().getMsgPayload().getUrl());
      params.put("previewUrl", (message.getPayload().getMsgPayload().getUrl()));

      if (message.getPayload().getMsgPayload().getCaption() != null) {
        params.put("caption", message.getPayload().getMsgPayload().getCaption());
      }
    } else if (message.getPayload().getType().equals("message")
        && (message.getPayload().getMsgPayload().getType().equals("file")
            || message.getPayload().getMsgPayload().getType().equals("audio")
            || message.getPayload().getMsgPayload().getType().equals("video"))) {

      params.put("type", message.getPayload().getMsgPayload().getType());
      params.put("url", message.getPayload().getMsgPayload().getUrl());
      if (message.getPayload().getMsgPayload().getFileName() != null) {
        params.put("fileName", message.getPayload().getMsgPayload().getFileName());
      }
      if (message.getPayload().getMsgPayload().getCaption() != null) {
        params.put("caption", message.getPayload().getMsgPayload().getCaption());
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
    headers.add("apikey", "8cfab6a264784290c2b736f2f53b51b4 ");
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
    GupshupMessageEntity msgEntity = msgRepo.findByPhoneNo(kafkaResponse.getPayload().getSource());

//    ObjectMapper mapper = new ObjectMapper();
//    String json = null;
    
    if (msgEntity == null) {
      msgEntity = new GupshupMessageEntity();
    }
    // json = mapper.writeValueAsString(body.getMessage());
    msgEntity.setPhoneNo(kafkaResponse.getPayload().getSource());
    msgEntity.setMessage(body.getNextMessage());
    msgEntity.setLastResponse(body.getCurrentIndex() == null ? true : false);

    // msgEntity.setPhoneNo(body.getMessage().getPayload().getSource());
    // msgEntity.setMsgId(body.getMessage().getPayload().getId());
    msgRepo.save(msgEntity);
  }

  private void replaceUserState(MS3Response body, MessageResponse kafkaResponse)
      throws JAXBException {
    GupshupStateEntity saveEntity = stateRepo.findByPhoneNo(kafkaResponse.getPayload().getSource());
    if (saveEntity == null) {
      saveEntity = new GupshupStateEntity();
    }
    saveEntity.setPhoneNo(kafkaResponse.getPayload().getSource());
    saveEntity.setPreviousPath(body.getCurrentIndex());
    saveEntity.setXmlPrevious(body.getCurrentResponseState());
    saveEntity.setBotFormName(null);
    stateRepo.save(saveEntity);
  }

}
