package com.samagra.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import javax.xml.bind.JAXBException;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.mock.web.MockMultipartFile;

@Slf4j
@Service
public class GupsShupWhatsappProviderService extends AbstractProvider implements IProvider {

  @Value("${provider.gupshup.whatsapp.appname}")
  private String gupshupWhatsappApp;
  private final static String GUPSHUP_OUTBOUND = "https://api.gupshup.io/sm/api/v1/msg";

  @Qualifier("rest")
  @Autowired
  private RestTemplate restTemplate;

  @Qualifier("custom")
  @Autowired
  private RestTemplate customRestTemplate;

  @Autowired
  private StateRepository stateRepo;

  @Autowired
  private MessageRepository msgRepo;

  private CredentialsProvider provider() {
    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("samagra", "impact@scale");
    provider.setCredentials(AuthScope.ANY, credentials);
    return provider;
  }

  private static HttpHeaders getVerifyHttpHeader4() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;

    MultipartInputStreamFileResource(InputStream inputStream, String filename) {
      super(inputStream);
      this.filename = filename;
    }

    @Override
    public String getFilename() {
      return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
      return -1; // we do not want to generally read the whole stream into memory ...
    }
  }

  @Override
  public void processInBoundMessage(MS3Response ms3Response, MessageResponse kafkaResponse)
      throws Exception {

    // db calls
    replaceUserState(ms3Response, kafkaResponse);
    appendNewResponse(ms3Response, kafkaResponse);

    boolean isLastResponse = ms3Response.getCurrentIndex().equals("endOfForm") ? true : false;

    if (isLastResponse) {
      // call to odk
      String ODK = "http://aggregate.cttsamagra.xyz:8080/Aggregate.html#submissions/filter///";
      String ODK2 = "http://aggregate.cttsamagra.xyz:8080/submission";
      HttpEntity<String> request = new HttpEntity<String>(getVerifyHttpHeader4());
      HttpEntity<String> response = customRestTemplate.exchange(ODK, HttpMethod.GET, request, String.class);
      System.out.println(new ObjectMapper().writeValueAsString(response));
      HttpHeaders headers = response.getHeaders();
      String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
      log.info("set-cookie {}", set_cookie);

      String fileName = "instance-"+  new Date().getTime() + ".xml";
      File f = new File(fileName);
      FileWriter myWriter = new FileWriter(fileName);
      myWriter.write(ms3Response.getCurrentResponseState());
      myWriter.close();
      File myObj = new File(fileName);
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

      // Took 3 hours to just resolve this.
      body.add("xml_submission_file", new FileSystemResource(myObj));

      HttpEntity<MultiValueMap<String, Object>> request2 = new HttpEntity<>(body, getVerifyHttpHeader2(set_cookie));
      restTemplate.getMessageConverters().add(getMappingJackson2HttpMessageConverter());
      HttpEntity<String> response2 = customRestTemplate.postForEntity(ODK2, request2, String.class);
      myObj.delete();
      log.info("response2 {}", response2);
      
      //db delete state
      GupshupMessageEntity msgEntity = msgRepo.findByPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
      msgRepo.delete(msgEntity);
      
      GupshupStateEntity saveEntity = stateRepo.findByPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
      stateRepo.delete(saveEntity);
      
    } else {
      sendGupshupWhatsAppOutBound(ms3Response, kafkaResponse);
    }
  }

  private static HttpHeaders getVerifyHttpHeader3() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    return headers;
  }

  private static HttpHeaders getVerifyHttpHeader2(String str) throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.add("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    headers.add("content-type", "multipart/form-data; boundary=----WebKitFormBoundarydaqjAWcH4XdvwPoz");
    headers.add("cookie", str);
    return headers;
  }



  private void sendGupshupWhatsAppOutBound(MS3Response ms3Response, MessageResponse value)
      throws Exception {
    String message = ms3Response.getNextMessage();


    HashMap<String, String> params = new HashMap<String, String>();
    params.put("channel", "whatsapp");
    params.put("source", "917834811114");
    params.put("destination", value.getPayload().getSender().getPhone());
    params.put("src.name", gupshupWhatsappApp);

    // params.put("type", "text");
    params.put("message", message);
    // params.put("isHSM", "false");

    String str2 =
        URLEncodedUtils.format(hashMapToNameValuePairList(params), '&', Charset.defaultCharset());

    System.out.println("Question for user: " + message);
    HttpEntity<String> request = new HttpEntity<String>(str2, getVerifyHttpHeader());
    restTemplate = new RestTemplate();
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
    headers.add("apikey", "8e455564878b4ca2ccb7b37f13ef9bfa");
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
    GupshupMessageEntity msgEntity = msgRepo.findByPhoneNo(kafkaResponse.getPayload().getSender().getPhone());

//    ObjectMapper mapper = new ObjectMapper();
//    String json = null;
    
    if (msgEntity == null) {
      msgEntity = new GupshupMessageEntity();
    }
    // json = mapper.writeValueAsString(body.getMessage());
    msgEntity.setPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
    msgEntity.setMessage(body.getNextMessage());
    msgEntity.setLastResponse(body.getCurrentIndex() == null ? true : false);

    // msgEntity.setPhoneNo(body.getMessage().getPayload().getSource());
    // msgEntity.setMsgId(body.getMessage().getPayload().getId());
    msgRepo.save(msgEntity);
  }

  private void replaceUserState(MS3Response body, MessageResponse kafkaResponse)
      throws JAXBException {
    GupshupStateEntity saveEntity = stateRepo.findByPhoneNo(kafkaResponse.getPayload().getSender().getPhone());
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
