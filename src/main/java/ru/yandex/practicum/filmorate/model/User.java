package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.anotation.NoSpaces;
import ru.yandex.practicum.filmorate.anotation.NotFuture;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"email"})
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private static final Logger log = LoggerFactory.getLogger(User.class);

    Long id;
    @Email @NotNull String email;

    @NotBlank(message = "Логин не может быть пустым")
    @NoSpaces
    String login;

    String name;

    @NotNull
    @NotFuture
    LocalDate birthday;

}

