package com.thoughtbend.enterprise.clientsvc.event;

import com.thoughtbend.enterprise.clientsvc.resource.ClientResource;

public class OutboundClientDataEvent {

	private final String eventName;
	private final ClientResource clientData;
	
	OutboundClientDataEvent(final String eventName, final ClientResource clientData) {
		this.eventName = eventName;
		this.clientData = clientData;
	}

	public String getEventName() {
		return eventName;
	}

	public ClientResource getClientData() {
		return clientData;
	}
	
}
