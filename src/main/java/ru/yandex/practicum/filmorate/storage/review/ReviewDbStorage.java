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

/**
 * Реализация хранилища отзывов для реляционной базы данных.
 * Использует JdbcTemplate для выполнения SQL-запросов.
 */
@Repository
@Primary
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Создает новый отзыв в базе данных.
     * Использует SimpleJdbcInsert для генерации ID и вставки данных.
     *
     * @param review объект отзыва для создания
     * @return созданный отзыв с присвоенным ID
     */
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
     //   parameters.put("useful", 0); УБРАЛА, тк оно должно вычисляться только при чтении из базы, иначе падают тесты

        // Выполняем вставку и получаем сгенерированный ID
        Long reviewId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        review.setReviewId(reviewId);
        review.setUseful(0);

        return review;
    }

    /**
     * Обновляет существующий отзыв в базе данных.
     *
     * @param review объект отзыва с обновленными данными
     * @return обновленный отзыв
     * @throws NotFoundException если отзыв с указанным ID не найден
     */
    @Override
    public Review update(Review review) {
        // SQL-запрос для обновления отзыва
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

        int updatedRows = jdbcTemplate.update(
                sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Отзыв с ID " + review.getReviewId() + " не найден.");
        }

        return findById(review.getReviewId()).orElseThrow();
    }

    /**
     * Удаляет отзыв по идентификатору.
     * Каскадно удаляет все связанные лайки и дизлайки благодаря foreign key constraints.
     *
     * @param id идентификатор отзыва для удаления
     */
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Находит отзыв по идентификатору.
     *
     * @param id идентификатор отзыва
     * @return Optional с найденным отзывом или empty, если отзыв не найден
     */
    @Override
    public Optional<Review> findById(Long id) {
        String sql = "SELECT r.*, " +
                "COALESCE(l.likes_count, 0) - COALESCE(d.dislikes_count, 0) as useful " +
                "FROM reviews r " +
                "LEFT JOIN (SELECT review_id, COUNT(*) as likes_count FROM review_likes GROUP BY review_id) l ON r.review_id = l.review_id " +
                "LEFT JOIN (SELECT review_id, COUNT(*) as dislikes_count FROM review_dislikes GROUP BY review_id) d ON r.review_id = d.review_id " +
                "WHERE r.review_id = ?";

        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, id);
        return reviews.stream().findFirst();
    }

    /**
     * Находит отзывы для конкретного фильма с ограничением количества.
     * Если filmId = null, возвращает все отзывы.
     * Результат сортируется по убыванию рейтинга полезности.
     *
     * @param filmId идентификатор фильма (может быть null)
     * @param count  максимальное количество возвращаемых отзывов
     * @return список отзывов, отсортированный по полезности
     */
    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        String sql = "SELECT r.*, " +
                "COALESCE(l.likes_count, 0) - COALESCE(d.dislikes_count, 0) as useful " +
                "FROM reviews r " +
                "LEFT JOIN (SELECT review_id, COUNT(*) as likes_count FROM review_likes GROUP BY review_id) l ON r.review_id = l.review_id " +
                "LEFT JOIN (SELECT review_id, COUNT(*) as dislikes_count FROM review_dislikes GROUP BY review_id) d ON r.review_id = d.review_id " +
                "WHERE (? IS NULL OR r.film_id = ?) " +
                "ORDER BY useful DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, filmId, count);
    }

    /**
     * Добавляет лайк отзыву от пользователя.
     * Автоматически удаляет дизлайк от этого пользователя, если он был.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    @Override
    public void addLike(Long reviewId, Long userId) {
        // Сначала удаляем возможный дизлайк
        removeDislike(reviewId, userId);

        // Проверяем, не поставил ли уже пользователь лайк
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count == 0) {
            String sql = "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, reviewId, userId);
        }
    }

    /**
     * Добавляет дизлайк отзыву от пользователя.
     * Автоматически удаляет лайк от этого пользователя, если он был.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    @Override
    public void addDislike(Long reviewId, Long userId) {
        // Сначала удаляем возможный лайк
        removeLike(reviewId, userId);

        // Проверяем, не поставил ли уже пользователь дизлайк
        String checkSql = "SELECT COUNT(*) FROM review_dislikes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count == 0) {
            String sql = "INSERT INTO review_dislikes (review_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, reviewId, userId);
        }
    }

    /**
     * Удаляет лайк отзыва от пользователя.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    @Override
    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, reviewId, userId);
        if (rowsAffected == 0) {
            //throw new NotFoundException("Лайк для отзыва не найден");
        }
    }

    /**
     * Удаляет дизлайк отзыва от пользователя.
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_dislikes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * Проверяет существование отзыва по идентификатору.
     *
     * @param id идентификатор отзыва
     * @return true если отзыв существует, false в противном случае
     */
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /**
     * Вспомогательный метод для маппинга строки ResultSet в объект Review.
     *
     * @param rs     ResultSet с данными отзыва
     * @param rowNum номер строки
     * @return объект Review
     * @throws SQLException в случае ошибок работы с ResultSet
     */
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
