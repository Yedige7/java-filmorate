package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService; // Для проверки существования пользователей
    private final FilmService filmService; // Для проверки существования фильмов
    private final EventService eventService; // Зависимость от ленты событий

    public Review create(Review review) {
        // Проверяем существование пользователя
        userService.getUserOrThrow(review.getUserId());
        // Проверяем существование фильма
        filmService.getFilmOrThrow(review.getFilmId());

        Review createdReview = reviewStorage.create(review);

        eventService.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(createdReview.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.ADD)
                .entityId(createdReview.getReviewId())
                .build());
        return createdReview;
    }

    public Review update(Review review) {
        // Проверяем существование отзыва перед обновлением
        findReviewId(review.getReviewId());

        Review updatedReview = reviewStorage.update(review);

        eventService.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(updatedReview.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.UPDATE)
                .entityId(updatedReview.getReviewId())
                .build());
        return updatedReview;
    }

    public void delete(Long id) {
        Review reviewToDelete = findReviewId(id);

        eventService.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(reviewToDelete.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.REMOVE)
                .entityId(id)
                .build());

        reviewStorage.delete(id);
    }

    public Review findReviewId(Long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + id + " не найден."));
    }

    public List<Review> getReviews(Long filmId, int count) {
        return reviewStorage.findByFilmId(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        userService.getUserOrThrow(userId);
        findReviewId(reviewId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        findReviewId(reviewId);
        userService.getUserOrThrow(userId); // Проверяем существование пользователя
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        reviewStorage.removeDislike(reviewId, userId);
    }
}
