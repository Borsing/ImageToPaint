package dev.borsing.imagetopaint.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidImageValidator.class)
public @interface ValidImage {

    String message() default "must be a readable PNG, JPEG or GIF image within the allowed dimensions";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
