package com.uci.outbound.Application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

import com.fasterxml.jackson.databind.JsonSerializable;
import com.uci.dao.service.HealthService;

@EnableKafka
@EnableAsync
@EnableReactiveCassandraRepositories("com.uci.dao")
@PropertySource("application-adapter.properties")
@PropertySource("application-messagerosa.properties")
@PropertySource("application.properties")
@SpringBootApplication
@ComponentScan(basePackages = {"com.uci.outbound", "com.uci.adapter", "com.uci.utils"})
public class Outbound {
	@Value("${spring.redis.database}")
	private String redisDb;
	
	@Value("${spring.redis.host}")
	private String redisHost;
	
	@Value("${spring.redis.port}")
	private String redisPort;
	
	public static void main(String[] args) {
		SpringApplication.run(Outbound.class, args);
	}
	
	@Bean
	public HealthService healthService() {
		return new HealthService();
	}

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		System.out.println("Redis property host: "+redisHost+", port: "+redisPort+", db index: "+redisDb);
		
	    JedisConnectionFactory jedisConFactory
	      = new JedisConnectionFactory();
	    jedisConFactory.setHostName(redisHost);
	    Integer port = Integer.parseInt(redisPort);
	    jedisConFactory.setPort(port);
	    Integer dbIndex = Integer.parseInt(redisDb);
	    jedisConFactory.setDatabase(dbIndex);
		jedisConFactory.getPoolConfig().setMaxIdle(30);
		jedisConFactory.getPoolConfig().setMinIdle(10);
	    return jedisConFactory;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
	    RedisTemplate<String, Object> template = new RedisTemplate<>();
	    template.setConnectionFactory(jedisConnectionFactory());
	    template.setKeySerializer(new StringRedisSerializer());
	    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
	    template.setEnableTransactionSupport(true);
	    return template;
	}
}
