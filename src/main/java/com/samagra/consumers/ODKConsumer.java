package com.samagra.consumers;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samagra.notification.Response.MS3Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RestController
public class ODKConsumer {

  @Autowired
  @Qualifier("custom")
  private RestTemplate customRestTemplate;

  @Autowired
  @Qualifier("rest")
  private RestTemplate restTemplate;


  private final static String ODK =
      "http://aggregate.cttsamagra.xyz:8080/Aggregate.html#submissions/filter///";

  private final static String ODK2 = "http://aggregate.cttsamagra.xyz:8080/submission";


  @KafkaListener(id = "odk-message", topics = "${odk-message}")
  public void consumeMessage(String message) throws Exception {
    log.info("im in odk message consumer");

    
    HttpEntity<String> request = new HttpEntity<String>(getVerifyHttpHeader());
    HttpEntity<String> response =
        customRestTemplate.exchange(ODK, HttpMethod.GET, request, String.class);
    log.info("response {}", new ObjectMapper().writeValueAsString(response));
    HttpHeaders headers = response.getHeaders();
    String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
    log.info("set-cookie {}", set_cookie);

    String fileName = "instance-" + new Date().getTime() + ".xml";
    FileWriter myWriter = new FileWriter(fileName);

    MS3Response ms3Response = new ObjectMapper().readValue(message, MS3Response.class);

    myWriter.write(ms3Response.getCurrentResponseState());
    myWriter.close();
    File myObj = new File(fileName);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    // Took 3 hours to just resolve this.
    body.add("xml_submission_file", new FileSystemResource(myObj));

    HttpEntity<MultiValueMap<String, Object>> request2 =
        new HttpEntity<>(body, getVerifyHttpHeader2(set_cookie));
    restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
    HttpEntity<String> response2 = customRestTemplate.postForEntity(ODK2, request2, String.class);
    myObj.delete();
    log.info("response2 {}", response2);


  }

  private static HttpHeaders getVerifyHttpHeader() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private static HttpHeaders getVerifyHttpHeader2(String str) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    headers.add("content-type",
        "multipart/form-data; boundary=----WebKitFormBoundarydaqjAWcH4XdvwPoz");
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
