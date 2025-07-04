package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.anotation.NoSpaces;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"email"})
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    @Email
    @NotNull
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @NoSpaces
    private String login;

    private String name;

    @NotNull
    @PastOrPresent
    private LocalDate birthday;

}

