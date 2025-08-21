package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с хранилищем отзывов.
 */
public interface ReviewStorage {
    /**
     * Создает новый отзыв в хранилище.
     *
     * @param review объект отзыва для создания (без ID)
     * @return созданный отзыв с присвоенным ID
     */
    Review create(Review review);

    /**
     * Обновляет существующий отзыв в хранилище.
     *
     * @param review объект отзыва с обновленными данными
     * @return обновленный отзыв
     */
    Review update(Review review);

    /**
     * Удаляет отзыв по идентификатору.
     *
     * @param id идентификатор отзыва для удаления
     */
    void delete(Long id);

    /**
     * Находит отзыв по идентификатору.
     *
     * @param id идентификатор отзыва
     * @return Optional с найденным отзывом или empty, если не найден
     */
    Optional<Review> findById(Long id);

    /**
     * Находит отзывы для конкретного фильма с ограничением количества.
     * Если filmId = null, возвращает все отзывы.
     * Результат сортируется по убыванию рейтинга полезности.
     *
     * @param filmId идентификатор фильма (может быть null)
     * @param count  максимальное количество возвращаемых отзывов
     * @return список отзывов, отсортированный по полезности
     */
    List<Review> findByFilmId(Long filmId, int count);

    /**
     * Добавляет лайк отзыву от пользователя.
     * Автоматически удаляет дизлайк от этого пользователя, если он был.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    void addLike(Long reviewId, Long userId);

    /**
     * Добавляет дизлайк отзыву от пользователя.
     * Автоматически удаляет лайк от этого пользователя, если он был.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    void addDislike(Long reviewId, Long userId);

    /**
     * Удаляет лайк отзыва от пользователя.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    void removeLike(Long reviewId, Long userId);

    /**
     * Удаляет дизлайк отзыва от пользователя.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    void removeDislike(Long reviewId, Long userId);

    /**
     * Проверяет существование отзыва по идентификатору.
     *
     * @param id идентификатор отзыва
     * @return true если отзыв существует, false в противном случае
     */
    boolean existsById(Long id);
}
