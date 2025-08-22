package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @NotNull(message = "ID режиссёра не может быть пустым")
    @Min(value = 1, message = "ID режиссёра должен быть положительным")
    private Long id;

    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
}