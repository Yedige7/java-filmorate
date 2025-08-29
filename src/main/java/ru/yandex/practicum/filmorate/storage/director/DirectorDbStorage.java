package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String SELECT_QUERY = "SELECT director_id, name FROM directors";
    private static final String SELECT_QUERY_BY_ID = "SELECT director_id, name FROM directors WHERE director_id = ?";
    private static final String DELETE_QUERY_BY_ID = "DELETE FROM directors WHERE director_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Long directorId = keyHolder.getKey().longValue();
        director.setId(directorId);
        return director;
    }

    @Override
    public Director update(Director director) {
        int rowsAffected = jdbcTemplate.update(UPDATE_QUERY, director.getName(), director.getId());
        if (rowsAffected == 0) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        return director;
    }


    @Override
    public List<Director> findAll() {
        return jdbcTemplate.query(SELECT_QUERY, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("name"));
            return director;
        });
    }

    @Override
    public Optional<Director> findById(long id) {
        return jdbcTemplate.query(SELECT_QUERY_BY_ID, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("name"));
            return director;
        }, id).stream().findFirst();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_QUERY_BY_ID, id);
    }
}