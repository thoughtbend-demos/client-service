package com.thoughtbend.enterprise.clientsvc.resource;

import javax.validation.constraints.NotBlank;

import org.springframework.core.style.DefaultToStringStyler;
import org.springframework.core.style.DefaultValueStyler;
import org.springframework.core.style.ToStringCreator;
import org.springframework.core.style.ToStringStyler;

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

	@Override
	public String toString() {
		
		final ToStringCreator tsc = new ToStringCreator(this, new DefaultToStringStyler(new DefaultValueStyler()));
		
		tsc.append("id", this.getId());
		tsc.append("name", this.getName());
		tsc.append("contactNumber", this.getContactNumber());
		tsc.append("clientExecutiveId", this.getClientExecutiveId());
		
		return tsc.toString();
	}

}
