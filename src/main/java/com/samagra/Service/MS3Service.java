package com.samagra.Service;

import java.io.StringWriter;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samagra.Entity.GupshupStateEntity;
import com.samagra.Repository.StateRepository;
import com.samagra.common.Request.MS3Request;
import com.samagra.common.Request.UserState;
import com.samagra.notification.Response.MS3Response;
import com.samagra.notification.Response.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ms3Service {

  @Autowired
  private StateRepository stateRepo;

  public MS3Response prepareMS3RequestAndGetResponse(MessageResponse value) throws Exception {
    MS3Request ms3Request = prapareMS3Request(value);

    // HttpEntity<MS3Request> request = new HttpEntity<>(ms3Request);
    //
    // ResponseEntity<MS3Response> ms3Response =
    // restTemplate.exchange(REQUEST_URI, HttpMethod.POST, request, MS3Response.class);

    String str = new String("{\n" + "   \"lastResponse\": false, \"messageRequest\": {\n"
        + "        \"app\": \"DemoApp\",\n" + "        \"timestamp\": 1580227766370,\n"
        + "        \"version\": 2,\n" + "        \"type\": \"message\",\n"
        + "        \"payload\": {\n"
        + "            \"id\": \"ABEGkYaYVSEEAhAL3SLAWwHKeKrt6s3FKB0c\",\n"
        + "            \"source\": \"917834811114\",\n"
        + "             \"destination\": \"9718908699\",\n " + "            \"type\": \"text\",\n"
        + "            \"payload\": {\n" + "                \"text\": \"Hi\"\n" + "            },\n"
        + "            \"sender\": {\n" + "                \"phone\": \"9415787824\",\n"
        + "                \"name\": \"Smit\"\n" + "            }\n" + "        }\n" + "    },\n"
        + "    \"userState\": \"<userstate><phoneno>9718908699</phoneno><questions><question1>value</question1></questions></userstate>\"\n"
        + "\n" + "}");

    ObjectMapper objectMapper = new ObjectMapper();
    MS3Response ms3Response = objectMapper.readValue(str, MS3Response.class);
    return ms3Response;
  }

  private MS3Request prapareMS3Request(MessageResponse value)
      throws JsonMappingException, JsonProcessingException, JAXBException {
    UserState userState = new UserState();

    GupshupStateEntity stateEntity = stateRepo.findByPhoneNo(value.getPayload().getPhone());
    if (stateEntity != null) {
      JSONObject jsonObject = new JSONObject(stateEntity.getState());
      userState.setPhoneno(jsonObject.getString("phone_no"));
      JSONObject mapObject = jsonObject.getJSONObject("questions");

      HashMap<String, String> result =
          new ObjectMapper().readValue(mapObject.toString(), HashMap.class);

      log.info("db result entity {} ", result);
      userState.setQuestions(result);
    }

    MS3Request ms3Request = new MS3Request();
    JAXBContext context = JAXBContext.newInstance(UserState.class);

    Marshaller marshaller = context.createMarshaller();

    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    StringWriter sw = new StringWriter();

    marshaller.marshal(userState, sw);
    ms3Request.setUserState(sw.toString());
    ms3Request.setMessageResponse(value);
    return ms3Request;
  }

}
