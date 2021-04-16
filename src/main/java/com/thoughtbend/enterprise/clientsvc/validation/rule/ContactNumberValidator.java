package com.thoughtbend.enterprise.clientsvc.validation.rule;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.thoughtbend.enterprise.clientsvc.validation.annotation.ContactNumberConstraint;

public class ContactNumberValidator implements 
				ConstraintValidator<ContactNumberConstraint, String> {

	@Override
	public boolean isValid(final String contactField, final ConstraintValidatorContext context) {
		
		return contactField != null && contactField.matches("[0-9]+")
		          && (contactField.length() > 9) && (contactField.length() < 14);
	}

	@Override
	public void initialize(final ContactNumberConstraint constraintAnnotation) {
		
	}

}
