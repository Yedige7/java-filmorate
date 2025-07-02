package ru.yandex.practicum.filmorate.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.util.PositiveDurationValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PositiveDurationValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveDuration {
    String message() default "Продолжительность фильма должна быть положительным числом.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
