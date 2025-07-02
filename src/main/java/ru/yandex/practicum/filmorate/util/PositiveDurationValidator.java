package ru.yandex.practicum.filmorate.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.anotation.PositiveDuration;

import java.time.Duration;

public class PositiveDurationValidator implements ConstraintValidator<PositiveDuration, Duration> {
    @Override
    public boolean isValid(Duration duration, ConstraintValidatorContext context) {
        if (duration == null) return true;
        return !duration.isNegative();
    }
}
