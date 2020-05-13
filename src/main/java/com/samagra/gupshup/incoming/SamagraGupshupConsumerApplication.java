package com.samagra.gupshup.incoming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableKafka
@EnableAsync
@ComponentScan(basePackages = {"com.samagra.*","Service"})
public class SamagraGupshupConsumerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SamagraGupshupConsumerApplication.class, args);
	}
}
