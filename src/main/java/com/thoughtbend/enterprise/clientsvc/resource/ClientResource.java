package com.thoughtbend.enterprise.clientsvc.resource;

import javax.validation.constraints.NotBlank;

import com.thoughtbend.enterprise.clientsvc.validation.annotation.ContactNumberConstraint;

public class ClientResource {

	private String id;
	
	@NotBlank
	private String name;
	
	@NotBlank
	@ContactNumberConstraint
	private String contactNumber;
	
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

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

}
