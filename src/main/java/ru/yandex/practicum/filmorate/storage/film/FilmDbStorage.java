package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.*;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, (int) film.getDuration().toMinutes());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();

        // Сохраняем жанры (если есть)
        saveGenres(newId, film.getGenres());

        // Возвращаем фильм с подгруженным mpa.name
        return findById(newId).orElseThrow(() -> new RuntimeException("Фильм не найден после добавления"));

    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int removed = jdbcTemplate.update(sql, filmId, userId);
        if (removed > 0) {
            log.info("Лайк пользователя {} удалён у фильма {}", userId, filmId);
        } else {
            log.warn("Не найден лайк пользователя {} для фильма {}", userId, filmId);
        }
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());
        log.info("Найдено фильмов: {}", films.size());
        return films;
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    private void updateGenres(Long filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM films_genres WHERE film_id = ?", filmId);
        saveGenres(filmId, genres);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                (int) film.getDuration().toMinutes(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new RuntimeException("Фильм с id " + film.getId() + " не найден для обновления");
        }

        // Обновляем жанры (удаляем старые и добавляем новые)
        updateGenres(film.getId(), film.getGenres());

        // Возвращаем фильм с подгруженным mpa.name
        return findById(film.getId()).orElseThrow(() -> new RuntimeException("Фильм не найден после обновления"));

    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "m.mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(Duration.ofMinutes(rs.getInt("duration")));

            Mpa mpa = new Mpa();
            mpa.setId(rs.getLong("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        }, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);

        // Подгрузка жанров
        String genreSql = "SELECT g.genre_id, g.name FROM genres g " +
                "JOIN films_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(genreSql,
                (rs, rowNum) -> new Genre(rs.getLong("genre_id"), rs.getString("name")), id));
        film.setGenres(genres);

        return Optional.of(film);
    }
}
