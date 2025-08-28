package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE review_id = ?";
    private static final String SELECT_QUERY = "SELECT r.*, " +
            "COALESCE(l.likes_count, 0) - COALESCE(d.dislikes_count, 0) as useful " +
            "FROM reviews r " +
            "LEFT JOIN (SELECT review_id, COUNT(*) as likes_count FROM review_likes GROUP BY review_id) l ON r.review_id = l.review_id " +
            "LEFT JOIN (SELECT review_id, COUNT(*) as dislikes_count FROM review_dislikes GROUP BY review_id) d ON r.review_id = d.review_id " +
            "WHERE r.review_id = ?";
    private static final String SELECT_QUERY_JOIN_LIMIT = "SELECT r.*, " +
            "COALESCE(l.likes_count, 0) - COALESCE(d.dislikes_count, 0) as useful " +
            "FROM reviews r " +
            "LEFT JOIN (SELECT review_id, COUNT(*) as likes_count FROM review_likes GROUP BY review_id) l ON r.review_id = l.review_id " +
            "LEFT JOIN (SELECT review_id, COUNT(*) as dislikes_count FROM review_dislikes GROUP BY review_id) d ON r.review_id = d.review_id " +
            "WHERE (? IS NULL OR r.film_id = ?) " +
            "ORDER BY useful DESC " +
            "LIMIT ?";
    private static final String SELECT_QUERY_REVIEW_LIKES = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String SELECT_QUERY_REVIEW_DISLIKES = "SELECT COUNT(*) FROM review_dislikes WHERE review_id = ? AND user_id = ?";
    private static final String INSERT_QUERY_REVIEW_LIKES = "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)";
    private static final String INSERT_QUERY_REVIEW_DISLIKES = "INSERT INTO review_dislikes (review_id, user_id) VALUES (?, ?)";
    private static final String DELETE_QUERY_REVIEW_LIKES = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String DELETE_QUERY_REVIEW_DISLIKES = "DELETE FROM review_dislikes WHERE review_id = ? AND user_id = ?";
    private static final String SELECT_QUERY_REVIEWS = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        // Создаем SimpleJdbcInsert для таблицы reviews
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");

        // Подготавливаем параметры для вставки
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("content", review.getContent());
        parameters.put("is_positive", review.getIsPositive());
        parameters.put("user_id", review.getUserId());
        parameters.put("film_id", review.getFilmId());

        // Выполняем вставку и получаем сгенерированный ID
        Long reviewId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        review.setReviewId(reviewId);
        review.setUseful(0);

        return review;
    }

    @Override
    public Review update(Review review) {
        // SQL-запрос для обновления отзыва
        int updatedRows = jdbcTemplate.update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Отзыв с ID " + review.getReviewId() + " не найден.");
        }

        return findById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(DELETE_QUERY, id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        List<Review> reviews = jdbcTemplate.query(SELECT_QUERY, this::mapRowToReview, id);
        return reviews.stream().findFirst();
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        return jdbcTemplate.query(SELECT_QUERY_JOIN_LIMIT, this::mapRowToReview, filmId, filmId, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        // Сначала удаляем возможный дизлайк
        removeDislike(reviewId, userId);

        // Проверяем, не поставил ли уже пользователь лайк
        Integer count = jdbcTemplate.queryForObject(SELECT_QUERY_REVIEW_LIKES, Integer.class, reviewId, userId);

        if (count == 0) {
            jdbcTemplate.update(INSERT_QUERY_REVIEW_LIKES, reviewId, userId);
        }
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        // Сначала удаляем возможный лайк
        removeLike(reviewId, userId);

        // Проверяем, не поставил ли уже пользователь дизлайк
        Integer count = jdbcTemplate.queryForObject(SELECT_QUERY_REVIEW_DISLIKES, Integer.class, reviewId, userId);

        if (count == 0) {
            jdbcTemplate.update(INSERT_QUERY_REVIEW_DISLIKES, reviewId, userId);
        }
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        int rowsAffected = jdbcTemplate.update(DELETE_QUERY_REVIEW_LIKES, reviewId, userId);
        if (rowsAffected == 0) {
            //throw new NotFoundException("Лайк для отзыва не найден");
        }
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {

        jdbcTemplate.update(DELETE_QUERY_REVIEW_DISLIKES, reviewId, userId);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(SELECT_QUERY_REVIEWS, Integer.class, id);
        return count != null && count > 0;
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
