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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RestController
@RequestMapping(value = "/odk")
public class ODKConsumer {

  @Autowired
  @Qualifier("custom")
  private RestTemplate customTemplate;

  @Autowired
  @Qualifier("rest")
  private RestTemplate restTemplate;


  private final static String ODK =
      "http://aggregate.cttsamagra.xyz:8080/Aggregate.html#submissions/filter///";

  private final static String ODK2 = "http://aggregate.cttsamagra.xyz:8080/submission";


  // @KafkaListener(id = "odk-message", topics = "${odk-message}")
  @RequestMapping(value = "/post", method = RequestMethod.POST)
  public void consumeMessage(String message) throws Exception {
    log.info("im in odk message consumer");

    // MS3Response finalResponse = new ObjectMapper().readValue(message, MS3Response.class);

    HttpEntity<String> request = new HttpEntity<String>(getVerifyHttpHeader());

    HttpEntity<String> response =
        customTemplate.exchange(ODK, HttpMethod.GET, request, String.class);
    HttpHeaders headers = response.getHeaders();
    String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
    log.info("set-cookie {}", set_cookie);

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("xml_submission_file", "file.xml");

    String str2 =
        URLEncodedUtils.format(hashMapToNameValuePairList(params), '&', Charset.defaultCharset());

    HttpEntity<String> request2 = new HttpEntity<>(str2, getVerifyHttpHeader2(set_cookie));
    restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
    HttpEntity<String> response2 = restTemplate.postForEntity(ODK2, request2, String.class);

    log.info("response2 {}", response2);


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
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private static HttpHeaders getVerifyHttpHeader2(String str) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    // headers.add("content-type",
    // "multipart/form-data; boundary=----WebKitFormBoundarydaqjAWcH4XdvwPoz");
    headers.add("cookie", str);
    return headers;
  }

  public static MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
        new MappingJackson2HttpMessageConverter();
    mappingJackson2HttpMessageConverter
        .setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED));
    return mappingJackson2HttpMessageConverter;
  }
}
