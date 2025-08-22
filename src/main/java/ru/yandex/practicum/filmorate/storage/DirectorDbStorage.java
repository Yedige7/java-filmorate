package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM directors ORDER BY director_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE director_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final DirectorMapper directorMapper;

    public List<Director> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, directorMapper);
    }

    public Optional<Director> findById(long id) {
        List<Director> list = jdbcTemplate.query(FIND_BY_ID_QUERY, directorMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0));
    }

    public Director create(Director director) {
        jdbcTemplate.update(INSERT_QUERY, director.getName());
        return director;
    }

    public Director update(Director director) {
        int rows = jdbcTemplate.update(UPDATE_QUERY, director.getName(), director.getId());
        if (rows == 0) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        return director;
    }

    public void delete(long id) {
        int rows = jdbcTemplate.update(DELETE_QUERY, id);
        if (rows == 0) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
    }
}