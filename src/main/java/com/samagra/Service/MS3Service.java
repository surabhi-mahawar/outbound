package com.samagra.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samagra.Entity.GupshupMessageEntity;
import com.samagra.Entity.GupshupStateEntity;
import com.samagra.Repository.MessageRepository;
import com.samagra.Repository.StateRepository;
import com.samagra.Request.MS3Request;
import com.samagra.Request.MessageRequest;
import com.samagra.Request.UserState;
import com.samagra.Response.InboundMessageResponse;
import com.samagra.Response.MS3Response;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MS3Service {
  private final String REQUEST_URI = "https://localhost";
  @Autowired
  private RestTemplate restTemplate;
  private final String GUPSHUP_OUTBOUND = "https://api.gupshup.io/sm/api/v1/msg";

  @Autowired
  private StateRepository stateRepo;

  @Autowired
  private MessageRepository msgRepo;


  public void processKafkaInResponse(InboundMessageResponse value)
      throws Exception {
    MS3Request ms3Request = prapareMS3Request(value);

    HttpEntity<MS3Request> request = new HttpEntity<>(ms3Request);

    // ResponseEntity<MS3Response> ms3Response =
    // restTemplate.exchange(REQUEST_URI, HttpMethod.POST, request, MS3Response.class);

    String str = new String("{\n" + "   \"lastResponse\": true, \"messageRequest\": {\n"
        + "        \"app\": \"DemoApp\",\n" + "        \"timestamp\": 1580227766370,\n"
        + "        \"version\": 2,\n" + "        \"type\": \"message\",\n"
        + "        \"payload\": {\n"
        + "            \"id\": \"ABEGkYaYVSEEAhAL3SLAWwHKeKrt6s3FKB0c\",\n"
        + "            \"source\": \"918x98xx21x4\",\n" + "            \"type\": \"text\",\n"
        + "            \"payload\": {\n" + "                \"text\": \"Hi\"\n" + "            },\n"
        + "            \"sender\": {\n" + "                \"phone\": \"918x98xx21x4\",\n"
        + "                \"name\": \"Smit\"\n" + "            }\n" + "        }\n" + "    },\n"
        + "    \"userState\": \"<userstate><phoneno>9718908699</phoneno><questions><question1>value</question1></questions></userstate>\"\n"
        + "\n" + "}");

    ObjectMapper objectMapper = new ObjectMapper();
    MS3Response ms3Response = objectMapper.readValue(str, MS3Response.class);

    replaceUserState(ms3Response);
    appendNewResponse(ms3Response);

    boolean isLastResponse = ms3Response.isLastResponse();
    isLastResponse = false;
    if (isLastResponse) {
      // call to odk
    } else {
      MessageRequest outBoundMessageRequest = ms3Response.getMessageRequest();
      HttpEntity<MessageRequest> outBound = new HttpEntity<>(outBoundMessageRequest,getVerifyHttpHeader());
       restTemplate.exchange(GUPSHUP_OUTBOUND, HttpMethod.POST, outBound, MS3Response.class);
    }
  }

  private HttpHeaders getVerifyHttpHeader() throws Exception {
    LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add(HttpHeaders.CACHE_CONTROL,"no-cache");
    map.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    map.add("apikey","c2ed3ece4e7c40eac0af0e012866e090");
    return new HttpHeaders(map);
  }
  

  private MS3Request prapareMS3Request(InboundMessageResponse value)
      throws JsonMappingException, JsonProcessingException, JAXBException {
    UserState userState = new UserState();

    GupshupStateEntity stateEntity = stateRepo.findByPhoneNo(value.getPayload().getPhone());
    if (stateEntity != null) {
      JSONObject jsonObject = new JSONObject(stateEntity.getState());
      userState.setPhoneno(jsonObject.getString("phone_no"));
      JSONObject mapObject = jsonObject.getJSONObject("questions");

      HashMap<String, String> result =
          new ObjectMapper().readValue(mapObject.toString(), HashMap.class);
      System.out.println(result);
      userState.setQuestions(result);
    }

    MS3Request ms3Request = new MS3Request();
    JAXBContext context = JAXBContext.newInstance(UserState.class);

    Marshaller marshaller = context.createMarshaller();

    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    StringWriter sw = new StringWriter();

    marshaller.marshal(userState, sw);
    ms3Request.setUserState(sw.toString());
    ms3Request.setInBoundResponse(value);
    return ms3Request;
  }


  private void appendNewResponse(MS3Response body) throws JsonProcessingException {
    GupshupMessageEntity msgEntity =
        msgRepo.findByPhoneNo(body.getMessageRequest().getPayload().getSource());
    ObjectMapper mapper = new ObjectMapper();
    String json = null;
    if (msgEntity == null) {
      msgEntity = new GupshupMessageEntity();
      json = mapper.writeValueAsString(body.getMessageRequest());
      msgEntity.setMessage(json);
      msgEntity.setPhoneNo(body.getMessageRequest().getPayload().getSource());
      msgEntity.setLastResponse(body.isLastResponse());
      msgEntity.setMsgId(body.getMessageRequest().getPayload().getId());
    } else {
      msgEntity.setPhoneNo(body.getMessageRequest().getPayload().getSource());
      msgEntity.setLastResponse(body.isLastResponse());

      json = mapper.writeValueAsString(body.getMessageRequest());
      msgEntity.setMessage(msgEntity.getMessage() + json);
      msgEntity.setMsgId(body.getMessageRequest().getPayload().getId());
    }
    msgRepo.save(msgEntity);
  }


  private void replaceUserState(MS3Response body) throws JAXBException {
    String incomingState = body.getUserState();
    GupshupStateEntity saveEntity =
        stateRepo.findByPhoneNo(body.getMessageRequest().getPayload().getSource());
    if (saveEntity == null) {
      saveEntity = new GupshupStateEntity();
      saveEntity.setState(incomingState);
      saveEntity.setPhoneNo(body.getMessageRequest().getPayload().getSource());
    } else {
      saveEntity.setState(incomingState);
    }
    stateRepo.save(saveEntity);
  }

  private UserState xmlStringToObject(String input) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(UserState.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    UserState employee = (UserState) jaxbUnmarshaller.unmarshal(new StringReader(input));
    return employee;
  }
}
