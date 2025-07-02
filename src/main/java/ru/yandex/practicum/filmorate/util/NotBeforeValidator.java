package ru.yandex.practicum.filmorate.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.anotation.NotBefore;

import java.time.LocalDate;

public class NotBeforeValidator implements ConstraintValidator<NotBefore, LocalDate> {
    private LocalDate minimumDate;

    @Override
    public void initialize(NotBefore constraintAnnotation) {
        minimumDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) return true;

        return !minimumDate.isAfter(date);
    }
}
