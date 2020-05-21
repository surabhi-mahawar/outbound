package com.samagra.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "bot_form")
public class BotEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name")
  private String formName;

  @Column(name = "welcome_message")
  private String welcomeMessage;

  @Column(name = "wrong_default_message")
  private String wrongDefaultMessage;
  
  @Column(name = "form_id")
  private Long formId;
  
}
