package ru.yandex.practicum.filmorate.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.anotation.NoSpaces;

public class NoSpacesValidator implements ConstraintValidator<NoSpaces, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !value.contains(" ");
    }
}
