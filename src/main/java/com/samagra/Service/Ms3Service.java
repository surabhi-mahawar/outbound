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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.samagra.Entity.GupshupStateEntity;
import com.samagra.Repository.StateRepository;
import com.samagra.common.Request.MS3Request;
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Ms3Service {

  private final static String REQUEST_URI = "http://localhost:8080/generate-message?";

  @Autowired
  @Qualifier("rest")
  private RestTemplate restTemplate;

  @Autowired
  private StateRepository stateRepo;

  public MS3Response prepareMS3RequestAndGetResponse(MessageResponse value) throws Exception {
    MS3Request ms3Request = prapareMS3Request(value);

    HashMap<String, String> map = new HashMap<String, String>();
    if (ms3Request.getPreviousPath() != null)
      map.put("previousPath", ms3Request.getPreviousPath());

    if (ms3Request.getCurrentAnswer() != null)
      map.put("currentAnswer", ms3Request.getCurrentAnswer());

    if (ms3Request.getInstanceXMlPrevious() != null)
      map.put("instanceXMlPrevious", ms3Request.getInstanceXMlPrevious());


    String urlParams =
        URLEncodedUtils.format(hashMapToNameValuePairList(map), '&', Charset.defaultCharset());

    // HttpEntity<String> request = new HttpEntity<String>(urlParams);

    restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
    MS3Response ms3Response = restTemplate.getForObject(REQUEST_URI + urlParams, MS3Response.class);

    return ms3Response;
  }

  private MS3Request prapareMS3Request(MessageResponse value)
      throws JsonMappingException, JsonProcessingException, JAXBException {
    String prevPath = null;
    String prevXMl = null;

    GupshupStateEntity stateEntity =
        stateRepo.findByPhoneNo(value.getPayload().getSender().getPhone());
    if (stateEntity != null) {
      prevXMl = stateEntity.getXmlPrevious();
      prevPath = stateEntity.getPreviousPath();

    }

    MS3Request ms3Request = new MS3Request();
    ms3Request.setCurrentAnswer(value.getPayload().getPayload().getText());
    ms3Request.setPreviousPath(prevPath);
    ms3Request.setInstanceXMlPrevious(prevXMl);
    return ms3Request;
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

  public static MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
        new MappingJackson2HttpMessageConverter();
    mappingJackson2HttpMessageConverter
        .setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));
    return mappingJackson2HttpMessageConverter;
  }

}
