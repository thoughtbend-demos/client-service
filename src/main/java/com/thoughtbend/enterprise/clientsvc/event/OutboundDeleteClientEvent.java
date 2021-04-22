package com.thoughtbend.enterprise.clientsvc.event;

import java.io.Serializable;

import com.thoughtbend.enterprise.clientsvc.resource.ClientResource;

public class OutboundDeleteClientEvent implements Serializable {

	private static final long serialVersionUID = -6635951207555599516L;
	
	private final String eventName = "DELETE_CLIENT";
	private final String clientId;
	private final ClientResource lastClientState;
	
	public OutboundDeleteClientEvent(final String clientId, final ClientResource lastClientState) {
		this.clientId = clientId;
		this.lastClientState = lastClientState;
	}

	public String getEventName() {
		return eventName;
	}

	public String getClientId() {
		return clientId;
	}

	public ClientResource getLastClientState() {
		return lastClientState;
	}
}
