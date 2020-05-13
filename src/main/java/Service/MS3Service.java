package Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import Entity.GupshupMessageEntity;
import Entity.GupshupStateEntity;
import Repository.MessageRepository;
import Repository.StateRepository;
import Request.InboundMessageResponse;
import Request.MS3Request;
import Request.MS3Response;
import Request.MessageRequest;
import Request.UserState;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MS3Service {
  private final String REQUEST_URI = "https://localhost";
  private RestTemplate restTemplate;

  @Autowired
  public StateRepository stateRepo;

  @Autowired
  public MessageRepository msgRepo;


  public void processKafkaInResponse(InboundMessageResponse value) {
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


  private MS3Request prapareMS3Request(InboundMessageResponse value) {
    UserState userState = null;
    GupshupStateEntity stateEntity = stateRepo.findByPhoneNo(value.getPayload().getPhone());
    if (stateEntity != null) {
      userState = stateEntity.getState();
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
    saveEntity.setState(incomingState);
    saveEntity.setPhoneNo(body.getMessageRequest().getPayload().getPhone());

    // save state and response to db
    stateRepo.save(saveEntity);

  }


}
