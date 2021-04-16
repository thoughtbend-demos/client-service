package com.thoughtbend.enterprise.clientsvc.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.thoughtbend.enterprise.clientsvc.validation.rule.ContactNumberValidator;

@Documented
@Constraint(validatedBy = ContactNumberValidator.class)
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface ContactNumberConstraint {

	String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
