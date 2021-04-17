package com.thoughtbend.enterprise.clientsvc.entity;

import org.springframework.core.style.DefaultToStringStyler;
import org.springframework.core.style.DefaultValueStyler;
import org.springframework.core.style.ToStringCreator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clientCollection")
public class ClientDocument {

	@Id
	private String dbId;
	
	@Indexed(unique = true)
	private String docId;
	
	@Indexed(unique = true)
	private String name;
	
	private String contactNumber;
	
	private String clientExecutiveId;

	public String getDbId() {
		return dbId;
	}

	public void setDbId(String dbId) {
		this.dbId = dbId;
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

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
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
		
		tsc.append("dbId", this.getDbId());
		tsc.append("docId", this.getDocId());
		tsc.append("name", this.getName());
		tsc.append("contactNumber", this.getContactNumber());
		tsc.append("clientExecutiveId", this.getClientExecutiveId());
		
		return tsc.toString();
	}
}
