package Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import Entity.GupshupStateEntity;
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


  public void processKafkaInResponse(InboundMessageResponse value) {
    UserState userState = null;
    GupshupStateEntity stateEntity = stateRepo.findByPhoneNo(value.getPayload().getPhone());
    if (stateEntity != null) {
      userState = stateEntity.getState();
    }
    MS3Request ms3Request = new MS3Request();
    ms3Request.setUserState(userState);
    ms3Request.setInBoundResponse(value);

    HttpEntity<MS3Request> request = new HttpEntity<>(ms3Request);

    ResponseEntity<MS3Response> ms3Response =
        restTemplate.exchange(REQUEST_URI, HttpMethod.POST, request, MS3Response.class);

    boolean isLastResponse = ms3Response.getBody().isLastResponse();
    if (isLastResponse) {
      
      // submit response to ODK
    } else {
      MessageRequest outBoundMessageRequest = ms3Response.getBody().getMessageRequest();
      // api call to gupshup.
    }

  }


}
