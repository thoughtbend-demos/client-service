package com.thoughtbend.enterprise.clientsvc.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedisClientEventPublisher implements ClientEventPublisher {

	private static final Logger LOG = LoggerFactory.getLogger(RedisClientEventPublisher.class);
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplatre;
	
	@Autowired
	private ChannelTopic clientDataEventTopic;
	
	@Autowired
	private ChannelTopic clientDeleteEventTopic;
	
	@Autowired
	private ChannelTopic clientErrorTopic;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Override
	public void publish(OutboundClientDataEvent event) {
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("Attempting to publish OutboundClientDataEvent to REDIS");
		}
		
		try {
			
			final String eventString = mapper.writeValueAsString(event);
			this.redisTemplatre.convertAndSend(this.clientDataEventTopic.getTopic(), eventString);
			
			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format("Successfully published OutboundClientDataEvent to REDIS [%1$s]", eventString));
			}
			
		} catch (JsonProcessingException ex) {
			this.handleJsonProcessingException(ex, event);
		}
	}

	@Override
	public void publish(OutboundDeleteClientEvent event) {
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("Attempting to publish OutboundClientDeleteEvent to REDIS");
		}
		
		try {
			
			final String eventString = mapper.writeValueAsString(event);
			this.redisTemplatre.convertAndSend(this.clientDeleteEventTopic.getTopic(), eventString);
			
			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format("Successfully published OutboundClientDataEvent to REDIS [%1$s]", eventString));
			}
			
		} catch (JsonProcessingException ex) {
			this.handleJsonProcessingException(ex, event);
		}
	}
	
	private void handleJsonProcessingException(final JsonProcessingException ex, Object event) {
		
		LOG.error(ex.getMessage(), ex);
		// When this happens, we should send notifications and publish to error channel some message.  As we wrote the 
		// data we don't want to fail out the complete transaction.
		this.redisTemplatre.convertAndSend(this.clientErrorTopic.getTopic(), event);
		
		if (LOG.isInfoEnabled()) {
			LOG.info("Successfully published event to ClientErrorTopic");
		}
	}

}
