package com.thoughtbend.enterprise.clientsvc.event;

import com.thoughtbend.enterprise.clientsvc.resource.ClientResource;

public enum OutboundClientDataEventType {

	NEW("NEW_CLIENT"), UPDATE("UPDATE_CLIENT");

	private final String name;

	private OutboundClientDataEventType(final String name) {
		this.name = name;
	}
	
	public OutboundClientDataEvent createEvent(final ClientResource clientState) {
		return new OutboundClientDataEvent(this.name, clientState);
	}
}
