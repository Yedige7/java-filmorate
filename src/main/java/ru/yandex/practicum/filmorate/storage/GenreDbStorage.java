package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class GenreDbStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_GENRE_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name"))
        );
    }

    public Optional<Genre> findById(long id) {
        List<Genre> list = jdbcTemplate.query(FIND_BY_GENRE_QUERY, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name")), id
        );
        return list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.getFirst());
    }

    /**
     * Получает все жанры для указанного фильма
     *
     * @param filmId идентификатор фильма
     * @return список жанров, связанных с фильмом
     */
    public List<Genre> getGenresByFilmId(long filmId) {
        String sql = """
                    SELECT g.genre_id, g.name\s
                    FROM genres g
                    JOIN film_genres fg ON g.genre_id = fg.genre_id
                    WHERE fg.film_id = ?
                    ORDER BY g.genre_id
               \s""";

        return jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
    }

    /**
     * Маппит строку результата в объект Genre
     */
    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(
                rs.getLong("genre_id"),
                rs.getString("name")
        );
    }
}