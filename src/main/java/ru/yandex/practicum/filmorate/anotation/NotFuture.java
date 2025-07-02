package ru.yandex.practicum.filmorate.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.util.NotFutureValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotFutureValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotFuture {
    String message() default "Дата рождения не может быть в будущем.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
