package com.samagra.Service;

import java.util.HashMap;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
  private RestTemplate restTemplate;

  @Autowired
  private StateRepository stateRepo;

  @Autowired
  private MessageRepository msgRepo;


  public void processKafkaInResponse(InboundMessageResponse value)
      throws JsonMappingException, JsonProcessingException {
    MS3Request ms3Request = prapareMS3Request(value);

    HttpEntity<MS3Request> request = new HttpEntity<>(ms3Request);

    ResponseEntity<MS3Response> ms3Response =
        restTemplate.exchange(REQUEST_URI, HttpMethod.POST, request, MS3Response.class);


    replaceUserState(ms3Response.getBody());
    appendNewResponse(ms3Response.getBody());

    boolean isLastResponse = ms3Response.getBody().isLastResponse();
    if (isLastResponse) {
      // call to odk
    } else {
      MessageRequest outBoundMessageRequest = ms3Response.getBody().getMessageRequest();
      // api call to gupshup.
    }
  }


  private MS3Request prapareMS3Request(InboundMessageResponse value)
      throws JsonMappingException, JsonProcessingException {
    UserState userState = new UserState();

    GupshupStateEntity stateEntity = stateRepo.findByPhoneNo(value.getPayload().getPhone());
    if (stateEntity != null) {
      JSONObject jsonObject = new JSONObject(stateEntity.getState());
      userState.setPhoneNo(jsonObject.getString("phone_no"));
      JSONObject mapObject = jsonObject.getJSONObject("questions");

      HashMap<String, String> result =
          new ObjectMapper().readValue(mapObject.toString(), HashMap.class);
      System.out.println(result);
      userState.setQuestions(result);
    }
    MS3Request ms3Request = new MS3Request();
    ms3Request.setUserState(userState);
    ms3Request.setInBoundResponse(value);
    return ms3Request;
  }


  private void appendNewResponse(MS3Response body) {
    GupshupMessageEntity msgEntity =
        msgRepo.findByPhoneNo(body.getMessageRequest().getPayload().getPhone());
    if (msgEntity == null) {
      msgEntity = new GupshupMessageEntity();
      msgEntity.setMessage(body.getMessageRequest().toString());
      msgEntity.setPhoneNo(body.getMessageRequest().getPayload().getPhone());
      msgEntity.setLastResponse(body.isLastResponse());
    } else {
      msgEntity.setLastResponse(body.isLastResponse());
      msgEntity.setMessage(msgEntity.getMessage() + body.getMessageRequest());
    }
    msgRepo.save(msgEntity);
  }


  private void replaceUserState(MS3Response body) {
    UserState incomingState = body.getUserState();
    GupshupStateEntity saveEntity = new GupshupStateEntity();
    saveEntity.setState(incomingState.toString());
    saveEntity.setPhoneNo(body.getMessageRequest().getPayload().getPhone());

    // save state and response to db
    stateRepo.save(saveEntity);

  }


}
