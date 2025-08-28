package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
public class Review {
    private Long reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым.")
    @Size(max = 5000, message = "Максимальная длина отзыва — 5000 символов.")
    private String content;


    @NotNull(message = "Тип отзыва (положительный/отрицательный) должен быть указан.")
    private Boolean isPositive;


    @NotNull(message = "Пользователь должен быть указан.")
    private Long userId;

    @NotNull(message = "Фильм должен быть указан.")
    private Long filmId;

    private int useful;


}
