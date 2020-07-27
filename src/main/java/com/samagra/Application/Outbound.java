package com.samagra.Application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableJpaRepositories("messagerosa.dao")
@EntityScan("messagerosa.dao")
@PropertySource("application-adapter.properties")
@PropertySource("application-messagerosa.properties")
@PropertySource("application.properties")
@ComponentScan(basePackages = {"com.samagra.Application","com.samagra"})
public class Outbound {
	public static void main(String[] args) {
		SpringApplication.run(Outbound.class, args);
	}
}
