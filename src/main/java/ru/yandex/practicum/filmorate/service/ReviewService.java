package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

/**
 * Сервисный слой для работы с отзывами.
 * Содержит бизнес-логику, валидацию и взаимодействие с другими сервисами.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService; // Для проверки существования пользователей
    private final FilmService filmService; // Для проверки существования фильмов

    /**
     * Создает новый отзыв с предварительными проверками.
     *
     * @param review объект отзыва для создания
     * @return созданный отзыв
     */
    public Review create(Review review) {
        // Проверяем существование пользователя
        userService.getUserOrThrow(review.getUserId());
        // Проверяем существование фильма
        filmService.getFilmOrThrow(review.getFilmId());
        return reviewStorage.create(review);
    }

    /**
     * Обновляет существующий отзыв.
     *
     * @param review объект отзыва с обновленными данными
     * @return обновленный отзыв
     */
    public Review update(Review review) {
        // Проверяем существование отзыва перед обновлением
        getreviewId(review.getReviewId());
        return reviewStorage.update(review);
    }

    /**
     * Удаляет отзыв по ID.
     *
     * @param id ID отзыва для удаления
     */
    public void delete(Long id) {
        reviewStorage.delete(id);
    }

    /**
     * Получает отзыв по ID.
     *
     * @param id ID отзыва
     * @return найденный отзыв
     * @throws NotFoundException если отзыв не существует
     */
    public Review getreviewId(Long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + id + " не найден."));
    }

    /**
     * Получает список отзывов с фильтрацией по фильму и ограничением количества.
     *
     * @param filmId ID фильма (может быть null для всех фильмов)
     * @param count  максимальное количество отзывов (по умолчанию 10)
     * @return список отзывов, отсортированный по полезности
     */
    public List<Review> getReviews(Long filmId, int count) {
        return reviewStorage.findByFilmId(filmId, count);
    }

    /**
     * Добавляет лайк отзыву от пользователя.
     *
     * @param reviewId ID отзыва
     * @param userId   ID пользователя
     * @throws NotFoundException если пользователь не существует
     */
    public void addLike(Long reviewId, Long userId) {
        userService.getUserOrThrow(userId); // Проверяем существование пользователя
        reviewStorage.addLike(reviewId, userId);
    }

    /**
     * Добавляет дизлайк отзыву от пользователя.
     *
     * @param reviewId ID отзыва
     * @param userId   ID пользователя
     * @throws NotFoundException если пользователь не существует
     */
    public void addDislike(Long reviewId, Long userId) {
        userService.getUserOrThrow(userId); // Проверяем существование пользователя
        reviewStorage.addDislike(reviewId, userId);
    }

    /**
     * Удаляет лайк отзыва от пользователя.
     *
     * @param reviewId ID отзыва
     * @param userId   ID пользователя
     */
    public void removeLike(Long reviewId, Long userId) {
        reviewStorage.removeLike(reviewId, userId);
    }

    /**
     * Удаляет дизлайк отзыва от пользователя.
     *
     * @param reviewId ID отзыва
     * @param userId   ID пользователя
     */
    public void removeDislike(Long reviewId, Long userId) {
        reviewStorage.removeDislike(reviewId, userId);
    }
}
