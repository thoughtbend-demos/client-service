package com.thoughtbend.enterprise.clientsvc.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultClientEventPublisher implements ClientEventPublisher {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultClientEventPublisher.class);
	
	@Override
	public void publish(OutboundClientDataEvent event) {
		LOG.info(String.format("Publishing NewClientEvent [%1$s]", event.toString()));
	}

}
