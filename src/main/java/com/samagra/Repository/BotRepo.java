package com.samagra.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.samagra.Entity.BotEntity;

@Repository
public interface BotRepo extends JpaRepository<BotEntity, Long> {
  BotEntity findByName(String name);
  BotEntity findByFormId(int i);
}
