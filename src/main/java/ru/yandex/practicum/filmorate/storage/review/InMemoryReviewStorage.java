package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryReviewStorage implements ReviewStorage {
    // Основное хранилище отзывов: ID -> Review
    private final Map<Long, Review> reviews = new HashMap<>();

    // Хранилище лайков: ID отзыва -> множество ID пользователей, поставивших лайк
    private final Map<Long, Set<Long>> reviewLikes = new HashMap<>();

    private final Map<Long, Set<Long>> reviewDislikes = new HashMap<>();

    private long idCounter = 1;

    @Override
    public Review create(Review review) {
        review.setReviewId(idCounter++);
        review.setUseful(0); // Начальный рейтинг полезности
        reviews.put(review.getReviewId(), review);
        // Инициализируем пустые множества для лайков и дизлайков этого отзыва
        reviewLikes.put(review.getReviewId(), new HashSet<>());
        reviewDislikes.put(review.getReviewId(), new HashSet<>());
        return review;
    }

    @Override
    public Review update(Review review) {
        Review existing = reviews.get(review.getReviewId());
        if (existing != null) {
            existing.setContent(review.getContent());
            existing.setIsPositive(review.getIsPositive());
        }
        return existing;
    }

    @Override
    public void delete(Long id) {
        reviews.remove(id);
        reviewLikes.remove(id);
        reviewDislikes.remove(id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(reviews.get(id));
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        return reviews.values().stream()
                // Фильтрация по filmId (если указан)
                .filter(review -> filmId == null || review.getFilmId().equals(filmId))
                // Сортировка по убыванию полезности
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                // Ограничение количества результатов
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        removeDislike(reviewId, userId); // Удаляем возможный дизлайк
        if (reviewLikes.get(reviewId).add(userId)) {
            updateUseful(reviewId, 1); // Увеличиваем рейтинг полезности
        }
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        removeLike(reviewId, userId); // Удаляем возможный лайк
        if (reviewDislikes.get(reviewId).add(userId)) {
            updateUseful(reviewId, -1); // Уменьшаем рейтинг полезности
        }
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        if (reviewLikes.get(reviewId).remove(userId)) {
            updateUseful(reviewId, -1);
        }
    }


    @Override
    public void removeDislike(Long reviewId, Long userId) {
        if (reviewDislikes.get(reviewId).remove(userId)) {
            updateUseful(reviewId, 1);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return reviews.containsKey(id);
    }

    private void updateUseful(Long reviewId, int delta) {
        Review review = reviews.get(reviewId);
        if (review != null) {
            review.setUseful(review.getUseful() + delta);
        }
    }
}
