package com.thoughtbend.enterprise.clientsvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thoughtbend.enterprise.clientsvc.event.ClientEventPublisher;
import com.thoughtbend.enterprise.clientsvc.event.RedisClientEventPublisher;

@Configuration
public class AppConfig {

	@Bean
	public ClientEventPublisher clientEventPublisher() {
		//return new DefaultClientEventPublisher();
		return new RedisClientEventPublisher();
	}
}
