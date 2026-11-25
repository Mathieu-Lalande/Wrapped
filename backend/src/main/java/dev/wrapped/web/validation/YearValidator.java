package dev.wrapped.web.validation;

import java.time.Year;
import java.time.ZoneOffset;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class YearValidator implements ConstraintValidator<ValidYear, Integer> {

    static final int MIN_YEAR = 2008;

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= MIN_YEAR && value <= Year.now(ZoneOffset.UTC).getValue();
    }
}
