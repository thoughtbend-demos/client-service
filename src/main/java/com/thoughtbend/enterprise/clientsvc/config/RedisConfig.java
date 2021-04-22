package com.thoughtbend.enterprise.clientsvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RedisConfig {

	private static final String CLIENT_DATA_EVENT_TOPIC = "clientDataEventTopic";
	private static final String CLIENT_DELETE_EVENT_TOPIC = "clientDeleteEventTopic";
	private static final String CLIENT_ERROR_TOPIC = "clientErrorTopic";
	
	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
		
		JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(config);

		return jedisConFactory;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}
	
	@Bean
	public ChannelTopic clientDataEventTopic() {
		return new ChannelTopic(CLIENT_DATA_EVENT_TOPIC);
	}
	
	@Bean
	public ChannelTopic clientDeleteEventTopic() {
		return new ChannelTopic(CLIENT_DELETE_EVENT_TOPIC);
	}
	
	@Bean
	public ChannelTopic clientErrorTopic() {
		return new ChannelTopic(CLIENT_ERROR_TOPIC);
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
