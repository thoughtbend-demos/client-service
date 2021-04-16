package com.thoughtbend.enterprise.clientsvc.event;

import com.thoughtbend.enterprise.clientsvc.resource.ClientResource;

public class OutboundDeleteClientEvent {

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
