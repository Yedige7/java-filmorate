package ru.yandex.practicum.filmorate.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.util.NoSpacesValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NoSpacesValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSpaces {
    String message() default "Логин не может содержать пробелы";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
