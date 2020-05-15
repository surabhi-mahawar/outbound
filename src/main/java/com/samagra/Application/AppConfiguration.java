package com.samagra.Application;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration
public class AppConfiguration {

  @Bean
  public RestTemplate getRestTemplate() {
    return new RestTemplate();
 }
}
