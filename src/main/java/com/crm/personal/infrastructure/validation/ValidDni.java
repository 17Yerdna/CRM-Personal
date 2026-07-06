package com.crm.personal.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Valida que el campo sea exactamente 8 dígitos numéricos (DNI peruano / estándar).
 */
@Documented
@Constraint(validatedBy = DniValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDni {
    String message() default "El DNI debe contener exactamente 8 dígitos numéricos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
