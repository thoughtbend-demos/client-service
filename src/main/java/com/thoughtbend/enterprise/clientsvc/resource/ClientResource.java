package com.thoughtbend.enterprise.clientsvc.resource;

import javax.validation.constraints.NotBlank;

public class ClientResource {

	private String id;
	
	@NotBlank
	private String name;
	
	private String clientExecutiveId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClientExecutiveId() {
		return clientExecutiveId;
	}

	public void setClientExecutiveId(String clientExecutiveId) {
		this.clientExecutiveId = clientExecutiveId;
	}

}
