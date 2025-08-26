package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
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
        return list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0));
    }
}