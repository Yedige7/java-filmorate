package ru.yandex.practicum.filmorate.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.util.NotBeforeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotBeforeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBefore {
    String message() default "Дата релиза — не раньше 28 декабря 1895 года;";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value() default "1895-12-28";
}
