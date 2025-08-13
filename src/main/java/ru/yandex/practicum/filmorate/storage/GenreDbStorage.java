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

    private final JdbcTemplate jdbcTemplate;

    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name"))
        );
    }

    public Optional<Genre> findById(long id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        List<Genre> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name")), id
        );
        return list.stream().findFirst();
    }
}