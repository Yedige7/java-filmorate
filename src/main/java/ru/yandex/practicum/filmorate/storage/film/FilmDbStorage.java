package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Override
    public Film create(Film film) {
        Long mpaId = film.getMpa().getId();
        Mpa mpa = mpaService.getById(mpaId);

        Set<Genre> uniqueGenres = film.getGenres().stream()
                .map(g -> genreService.findById(g.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(uniqueGenres);

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            if (film.getDuration() != null) {
                ps.setInt(4, (int) film.getDuration().toMinutes());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            if (mpa != null && mpa.getId() != null) {
                ps.setLong(5, mpa.getId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();

        // Сохраняем жанры
        if (!film.getGenres().isEmpty()) {
            saveGenres(newId, film.getGenres());
        }

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
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
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

        if (genres.size() > 0) {
            String sql = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
            Genre[] g = genres.toArray(new Genre[genres.size()]);
            jdbcTemplate.batchUpdate(
                    sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, filmId);
                            ps.setLong(2, g[i].getId());
                        }

                        public int getBatchSize() {
                            return genres.size();
                        }
                    });
        }

    }

    private void updateGenres(Long filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM films_genres WHERE film_id = ?", filmId);
        saveGenres(filmId, genres);
    }

    @Override
    public Film update(Film film) {
        Film existingFilm = findById(film.getId())
                .orElseThrow(() -> new RuntimeException("Фильм с id " + film.getId() + " не найден"));

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        Mpa mpa = mpaService.getById(film.getMpa().getId());
        film.setMpa(mpa);
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                (int) film.getDuration().toMinutes(),
                mpa.getId(),
                film.getId()
        );


        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с id=" + film.getId() + " не найден");
        }


        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateGenres(film.getId(), film.getGenres());
        }

        String selectGenres = "SELECT g.genre_id, g.name FROM genres g " +
                "JOIN films_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id=? ORDER BY g.genre_id";
        List<Genre> genreList = jdbcTemplate.query(selectGenres, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name")), film.getId());
        film.setGenres(new LinkedHashSet<>(genreList));

        return film;
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
            Date releaseDate = rs.getDate("release_date");
            film.setReleaseDate(releaseDate != null ? releaseDate.toLocalDate() : null);
            film.setDuration(Duration.ofMinutes(rs.getInt("duration")));

            Long mpaId = rs.getObject("mpa_id", Long.class); // null-safe
            String mpaName = rs.getString("mpa_name"); // вернёт null, если нет
            Mpa mpa = null;
            if (mpaId != null) {
                mpa = new Mpa();
                mpa.setId(mpaId);
                mpa.setName(mpaName);
            }
            film.setMpa(mpa);

            return film;
        }, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);

        String genreSql = "SELECT g.genre_id, g.name FROM genres g " +
                "JOIN films_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? order by g.genre_id";
        List<Genre> genreList = jdbcTemplate.query(genreSql,
                (rs, rowNum) -> new Genre(rs.getLong("genre_id"), rs.getString("name")), id);
        film.setGenres(genreList != null ? new LinkedHashSet<>(genreList) : new LinkedHashSet<>());

        return Optional.of(film);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                "       COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN LIKES fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id, m.name " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, new Object[]{count}, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(Duration.ofMinutes(rs.getInt("duration")));

            Long mpaId = rs.getLong("mpa_id");
            if (mpaId != 0) {
                Mpa mpa = new Mpa();
                mpa.setId(mpaId);
                mpa.setName(rs.getString("mpa_name"));
                film.setMpa(mpa);
            }

            film.setLikes(new HashSet<>());

            return film;
        });
    }
}
