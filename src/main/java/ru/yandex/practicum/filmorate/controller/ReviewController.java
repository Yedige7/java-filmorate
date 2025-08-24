package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;

/**
 * REST контроллер для работы с отзывами.
 * Обрабатывает HTTP-запросы и делегирует выполнение сервисному слою.
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    /**
     * Создает новый отзыв.
     * POST /reviews
     * @param review объект отзыва из тела запроса
     * @return созданный отзыв
     */
    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    /**
     * Обновляет существующий отзыв.
     * PUT /reviews
     * @param review объект отзыва с обновленными данными
     * @return обновленный отзыв
     */
    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        return reviewService.update(review);
    }

    /**
     * Удаляет отзыв по ID.
     * DELETE /reviews/{id}
     * @param id ID отзыва для удаления
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }

    /**
     * Получает отзыв по ID.
     * GET /reviews/{id}
     * @param id ID отзыва
     * @return найденный отзыв
     */
    @GetMapping("/{id}")
    public Review getById(@PathVariable Long id) {
        return reviewService.getreviewId(id);
    }

    /**
     * Получает список отзывов с возможностью фильтрации по фильму.
     * GET /reviews?filmId={filmId}&count={count}
     * @param filmId ID фильма для фильтрации (опционально)
     * @param count количество возвращаемых отзывов (по умолчанию зададим 10)
     * @return список отзывов
     */
    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        return reviewService.getReviews(filmId, count);
    }

    /**
     * Добавляет лайк отзыву от пользователя.
     * PUT /reviews/{id}/like/{userId}
     * @param id ID отзыва
     * @param userId ID пользователя
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addLike(id, userId);
    }

    /**
     * Добавляет дизлайк отзыву от пользователя.
     * PUT /reviews/{id}/dislike/{userId}
     * @param id ID отзыва
     * @param userId ID пользователя
     */
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addDislike(id, userId);
    }

    /**
     * Удаляет лайк отзыва от пользователя.
     * DELETE /reviews/{id}/like/{userId}
     * @param id ID отзыва
     * @param userId ID пользователя
     */
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeLike(id, userId);
    }

    /**
     * Удаляет дизлайк отзыва от пользователя.
     * DELETE /reviews/{id}/dislike/{userId}
     * @param id ID отзыва
     * @param userId ID пользователя
     */
    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeDislike(id, userId);
    }
}
