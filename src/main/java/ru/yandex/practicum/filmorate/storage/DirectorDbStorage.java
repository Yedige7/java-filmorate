package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE director_id = ?";

    @Override
    public List<Director> findAll() {
        List<Director> directors = jdbcTemplate.query(FIND_ALL_QUERY, new DirectorMapper());
        log.info("Found {} directors: {}", directors.size(), directors);
        return directors;
    }

    @Override
    public Optional<Director> findById(long id) {
        try {
            Director director = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, new DirectorMapper(), id);
            return Optional.ofNullable(director);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Director create(Director director) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", director.getName());
        Number key = jdbcInsert.executeAndReturnKey(parameters);
        director.setId(key.longValue());
        log.info("Created director with ID: {}", director.getId());
        return director;
    }

    @Override
    public Director update(Director director) {
        int rows = jdbcTemplate.update(UPDATE_QUERY, director.getName(), director.getId());
        if (rows == 0) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        return director;
    }

    @Override
    public void delete(long id) {
        int rows = jdbcTemplate.update(DELETE_QUERY, id);
        if (rows == 0) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
    }

    private static class DirectorMapper implements RowMapper<Director> {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("name"));
            return director;
        }
    }
}