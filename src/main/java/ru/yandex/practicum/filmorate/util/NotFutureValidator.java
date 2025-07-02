package ru.yandex.practicum.filmorate.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.anotation.NotFuture;

import java.time.LocalDate;

public class NotFutureValidator implements ConstraintValidator<NotFuture, LocalDate> {
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) return true;
        return !date.isAfter(LocalDate.now());
    }
}
