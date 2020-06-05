package com.samagra.consumers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samagra.notification.Response.MS3Response;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class OutBoundGupshupConsumer {
  @Autowired
  @Qualifier("rest")
  private RestTemplate restTemplate;

  @Value("${provider.gupshup.whatsapp.appname}")
  private String gupshupWhatsappApp;

  @Value("{provider.gupshup.whatsapp.apikey}")
  private String gsApiKey;

  private final static String GUPSHUP_OUTBOUND = "https://api.gupshup.io/sm/api/v1/msg";

  @KafkaListener(id = "outbound", topics = "${gs-whatsapp-outbound-message")
  private void sendGupshupWhatsAppOutBound(String ms3Response) throws Exception {
    MS3Response response = new ObjectMapper().readValue(ms3Response, MS3Response.class);

    String nextMessage = response.getNextMessage();

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("channel", "whatsapp");
    params.put("source", "917834811114");
    params.put("destination", "9718908699");
    params.put("src.name", "demobb");
    // params.put("type", "text");
    params.put("message", nextMessage);
    // params.put("isHSM", "false");

    String str2 =
        URLEncodedUtils.format(hashMapToNameValuePairList(params), '&', Charset.defaultCharset());

    log.info("Question for user: {}", nextMessage);
    HttpEntity<String> request = new HttpEntity<String>(str2, getVerifyHttpHeader());
    restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
    restTemplate.postForObject(GUPSHUP_OUTBOUND, request, String.class);
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

  private HttpHeaders getVerifyHttpHeader() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("Cache-Control", "no-cache");
    headers.add("apikey", gsApiKey);
    return headers;
  }

  public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
        new MappingJackson2HttpMessageConverter();
    mappingJackson2HttpMessageConverter
        .setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));
    return mappingJackson2HttpMessageConverter;
  }
}
