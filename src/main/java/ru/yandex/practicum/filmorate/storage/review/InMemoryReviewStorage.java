package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory реализация хранилища отзывов.
 * Хранит данные в оперативной памяти (HashMap).
 * При перезапуске приложения все данные теряются.
 */
@Component
public class InMemoryReviewStorage implements ReviewStorage {
    // Основное хранилище отзывов: ID -> Review
    private final Map<Long, Review> reviews = new HashMap<>();

    // Хранилище лайков: ID отзыва -> множество ID пользователей, поставивших лайк
    private final Map<Long, Set<Long>> reviewLikes = new HashMap<>();

    // Хранилище дизлайков: ID отзыва -> множество ID пользователей, поставивших дизлайк
    private final Map<Long, Set<Long>> reviewDislikes = new HashMap<>();

    // Счетчик для генерации уникальных ID отзывов
    private long idCounter = 1;

    /**
     * Создает новый отзыв с автоматической генерацией ID.
     * Инициализирует рейтинг полезности нулем и пустые множества для лайков/дизлайков.
     */
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

    /**
     * Обновляет существующий отзыв.
     * Можно изменить только содержание и тип отзыва (положительный/отрицательный).
     * Нельзя изменить автора, фильм или рейтинг полезности через этот метод.
     */
    @Override
    public Review update(Review review) {
        Review existing = reviews.get(review.getReviewId());
        if (existing != null) {
            existing.setContent(review.getContent());
            existing.setIsPositive(review.getIsPositive());
        }
        return existing;
    }

    /**
     * Удаляет отзыв и все связанные с ним лайки/дизлайки.
     */
    @Override
    public void delete(Long id) {
        reviews.remove(id);
        reviewLikes.remove(id);
        reviewDislikes.remove(id);
    }

    /**
     * Возвращает отзыв по ID в виде Optional для безопасной обработки.
     */
    @Override
    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(reviews.get(id));
    }

    /**
     * Находит отзывы с фильтрацией по фильму и ограничением количества.
     * Если filmId = null, возвращает отзывы для всех фильмов.
     * Сортировка по убыванию рейтинга полезности (самые полезные сначала).
     */
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

    /**
     * Добавляет лайк отзыву от пользователя.
     * Автоматически удаляет дизлайк от этого пользователя, если он существовал.
     * Увеличивает рейтинг полезности на 1.
     */
    @Override
    public void addLike(Long reviewId, Long userId) {
        removeDislike(reviewId, userId); // Удаляем возможный дизлайк
        if (reviewLikes.get(reviewId).add(userId)) {
            updateUseful(reviewId, 1); // Увеличиваем рейтинг полезности
        }
    }

    /**
     * Добавляет дизлайк отзыву от пользователя.
     * Автоматически удаляет лайк от этого пользователя, если он существовал.
     * Уменьшает рейтинг полезности на 1.
     */
    @Override
    public void addDislike(Long reviewId, Long userId) {
        removeLike(reviewId, userId); // Удаляем возможный лайк
        if (reviewDislikes.get(reviewId).add(userId)) {
            updateUseful(reviewId, -1); // Уменьшаем рейтинг полезности
        }
    }

    /**
     * Удаляет лайк отзыва от пользователя.
     * Уменьшает рейтинг полезности на 1.
     */
    @Override
    public void removeLike(Long reviewId, Long userId) {
        if (reviewLikes.get(reviewId).remove(userId)) {
            updateUseful(reviewId, -1);
        }
    }

    /**
     * Удаляет дизлайк отзыва от пользователя.
     * Увеличивает рейтинг полезности на 1.
     */
    @Override
    public void removeDislike(Long reviewId, Long userId) {
        if (reviewDislikes.get(reviewId).remove(userId)) {
            updateUseful(reviewId, 1);
        }
    }

    /**
     * Проверяет существование отзыва по ID.
     */
    @Override
    public boolean existsById(Long id) {
        return reviews.containsKey(id);
    }

    /**
     * Вспомогательный метод для обновления рейтинга полезности отзыва.
     *
     * @param reviewId ID отзыва
     * @param delta    величина изменения (+1 или -1)
     */
    private void updateUseful(Long reviewId, int delta) {
        Review review = reviews.get(reviewId);
        if (review != null) {
            review.setUseful(review.getUseful() + delta);
        }
    }
}
