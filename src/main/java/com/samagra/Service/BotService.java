package com.samagra.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.samagra.Entity.BotEntity;
import com.samagra.Repository.BotRepo;

@Service
public class BotService {

  @Autowired
  private BotRepo formRepo;

  public BotEntity getFormEntity(String name) {
    return formRepo.findByFormName(name);
  }


}
