package com.samagra.Application;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.samagra.Entity.BotEntity;
import com.samagra.Repository.BotRepo;
import com.samagra.common.Request.FormRequest;

@RestController
@RequestMapping(value = "/form")
public class FormController {

  @Autowired
  private BotRepo formRepo;

  @RequestMapping(value = "/post", method = RequestMethod.POST)
  public void createNewForm(@Valid @RequestBody FormRequest message)
      throws JsonProcessingException {
    formRepo.save(convertRequestToEntity(message));
  }

  private BotEntity convertRequestToEntity(@Valid FormRequest message) {
    BotEntity entity = new BotEntity();
    entity.setFormName(message.getFormName());
    entity.setWelcomeMessage(message.getWelcomeMessage());
    entity.setWrongDefaultMessage(message.getWrongDefaultMessage());
    return entity;
  }
}
