package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Класс, представляющий сущность "Отзыв" в системе.
 * Отзыв содержит оценку (полезно/бесполезно) пользователя о фильме и тип отзыва (негативный/положительный).
 */
@Data
@Builder
public class Review {
    private Long reviewId; // Уникальный идентификатор отзыва

    /**
     * Текстовое содержание отзыва. Обязательное поле с ограничением длины.
     */
    @NotBlank(message = "Содержание отзыва не может быть пустым.")
    @Size(max = 5000, message = "Максимальная длина отзыва — 5000 символов.")
    private String content;

    /**
     * Флаг, указывающий тип отзыва:
     * true - положительный отзыв, false - отрицательный отзыв.
     * Обязательное поле.
     */
    @NotNull(message = "Тип отзыва (положительный/отрицательный) должен быть указан.")
    private Boolean isPositive;

    /**
     * Идентификатор пользователя-автора отзыва. Обязательное поле.
     */
    @NotNull(message = "Пользователь должен быть указан.")
    private Long userId;

    /**
     * Идентификатор фильма, к которому относится отзыв. Обязательное поле.
     */
    @NotNull(message = "Фильм должен быть указан.")
    private Long filmId;

    /**
     * Рейтинг полезности отзыва. Рассчитывается автоматически на основе лайков/дизлайков.
     * Изначально равен 0 при создании отзыва.
     */
    private int useful;


}
