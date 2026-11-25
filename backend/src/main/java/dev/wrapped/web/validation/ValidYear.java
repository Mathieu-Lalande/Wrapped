package dev.wrapped.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Année comprise entre {@value YearValidator#MIN_YEAR} (création de GitHub) et l'année courante (UTC).
 * {@code null} est considéré comme valide (paramètre optionnel).
 */
@Documented
@Constraint(validatedBy = YearValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidYear {

    String message() default "doit être comprise entre 2008 et l'année en cours";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
