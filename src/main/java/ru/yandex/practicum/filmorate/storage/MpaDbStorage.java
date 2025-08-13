package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY MPA_ID";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getLong("mpa_id"), rs.getString("name"))
        );
    }

    public Optional<Mpa> findById(long id) {
        String sql = "SELECT * FROM mpa WHERE MPA_ID = ?";
        List<Mpa> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getLong("mpa_id"), rs.getString("name")), id
        );
        return list.stream().findFirst();
    }
}
