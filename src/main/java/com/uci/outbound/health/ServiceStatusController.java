package com.uci.outbound.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uci.dao.service.HealthService;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/service")
public class ServiceStatusController {

	@Autowired 
	private HealthService healthService;

    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = { "application/json", "text/json" })
    public ResponseEntity<JsonNode> statusCheck() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree("{\"id\":\"api.content.service.health\",\"ver\":\"3.0\",\"ts\":null,\"params\":{\"resmsgid\":null,\"msgid\":null,\"err\":null,\"status\":\"successful\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"healthy\":true}}");
        return ResponseEntity.ok(json);
    }
    
    @RequestMapping(value = "/health/cassandra", method = RequestMethod.GET, produces = { "application/json", "text/json" })
    public ResponseEntity<JsonNode> cassandraStatusCheck() throws IOException, JsonProcessingException {
    	JsonNode jsonNode = getResponseJsonNode();
    	((ObjectNode) jsonNode).put("result", healthService.getCassandraHealthNode());
        
        return ResponseEntity.ok(jsonNode);
    }
    
    @RequestMapping(value = "/health/kafka", method = RequestMethod.GET, produces = { "application/json", "text/json" })
    public ResponseEntity<JsonNode> kafkaStatusCheck() throws IOException, JsonProcessingException {
    	JsonNode jsonNode = getResponseJsonNode();
    	((ObjectNode) jsonNode).put("result", healthService.getKafkaHealthNode());
        
        return ResponseEntity.ok(jsonNode);
    }
    
    @RequestMapping(value = "/health/campaign", method = RequestMethod.GET, produces = { "application/json", "text/json" })
    public ResponseEntity<JsonNode> campaignUrlStatusCheck() throws JsonProcessingException, IOException {
    	JsonNode jsonNode = getResponseJsonNode();
        ((ObjectNode) jsonNode).put("result", healthService.getCampaignUrlHealthNode());
        
        return ResponseEntity.ok(jsonNode);
    }
    
    /**
     * Returns json node for service response
     * 
     * @return JsonNode
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    private JsonNode getResponseJsonNode() throws JsonMappingException, JsonProcessingException {
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode jsonNode = mapper.readTree("{\"id\":\"api.content.service.health\",\"ver\":\"3.0\",\"ts\":null,\"params\":{\"resmsgid\":null,\"msgid\":null,\"err\":null,\"status\":\"successful\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"healthy\":false}}");
        return jsonNode;
    }
}
