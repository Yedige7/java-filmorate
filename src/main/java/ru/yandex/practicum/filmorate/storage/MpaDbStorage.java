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
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY MPA_ID";
    private static final String FIND_BY_MPA_QUERY = "SELECT * FROM mpa WHERE MPA_ID = ?";
    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, (rs, rowNum) -> new Mpa(rs.getLong("mpa_id"), rs.getString("name")));
    }

    public Optional<Mpa> findById(long id) {
        List<Mpa> list = jdbcTemplate.query(FIND_BY_MPA_QUERY, (rs, rowNum) -> new Mpa(rs.getLong("mpa_id"), rs.getString("name")), id);
        return list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0));
    }
}
