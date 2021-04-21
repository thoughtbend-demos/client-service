package com.thoughtbend.enterprise.clientsvc.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisClientEventPublisher implements ClientEventPublisher {

	@Autowired
	private RedisTemplate<String, Object> redisTemplatre;
	
	@Override
	public void publish(OutboundClientDataEvent event) {
		
		ListOperations<String, Object> listOps = this.redisTemplatre.opsForList();
		listOps.leftPush("clientDataEvent", event);
	}

	@Override
	public void publish(OutboundDeleteClientEvent event) {
		ListOperations<String, Object> listOps = this.redisTemplatre.opsForList();
		listOps.leftPush("clientDeleteEvent", event);
	}

}
