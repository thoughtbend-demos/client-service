package com.thoughtbend.enterprise.clientsvc.event;

import java.io.Serializable;

import com.thoughtbend.enterprise.clientsvc.resource.ClientResource;

public class OutboundClientDataEvent implements Serializable {

	private static final long serialVersionUID = 8112274109539563570L;
	
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
