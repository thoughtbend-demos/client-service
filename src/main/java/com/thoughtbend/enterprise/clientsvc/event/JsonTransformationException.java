package com.thoughtbend.enterprise.clientsvc.event;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class JsonTransformationException extends RuntimeException {

	private static final long serialVersionUID = -6879516594259993657L;

}
