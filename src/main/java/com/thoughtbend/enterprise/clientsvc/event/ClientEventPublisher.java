package com.thoughtbend.enterprise.clientsvc.event;

public interface ClientEventPublisher {

	public void publish(OutboundClientDataEvent event);
}
