package com.uci.outbound.Application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

import com.uci.dao.service.HealthService;

@EnableKafka
@EnableAsync
@EnableReactiveCassandraRepositories("com.uci.dao")
@EntityScan("com.uci.dao")
@PropertySource("application-adapter.properties")
@PropertySource("application-messagerosa.properties")
@PropertySource("application.properties")
@SpringBootApplication
@ComponentScan(basePackages = {"com.uci.outbound", "com.uci.adapter", "com.uci.utils"})
public class Outbound {
	public static void main(String[] args) {
		SpringApplication.run(Outbound.class, args);
	}
	
	@Bean
	public HealthService healthService() {
		return new HealthService();
	}
}
