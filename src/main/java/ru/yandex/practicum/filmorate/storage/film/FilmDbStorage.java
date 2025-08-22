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
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String DELETE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_LIKES_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " + "m.mpa_id, m.name AS mpa_name " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id ";
    private static final String INSERT_FILMS_GENRES_QUERY = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_QUERY_FROM_FILMS_GENRES = "DELETE FROM films_genres WHERE film_id = ?";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String FIND_GENRES_QUERY = "SELECT g.genre_id, g.name FROM genres g " + "JOIN films_genres fg ON g.genre_id = fg.genre_id " + "WHERE fg.film_id=? ORDER BY g.genre_id";
    private static final String FIND_FILMS_BY_JOIN_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " + "m.mpa_id, m.name AS mpa_name " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " + "WHERE f.film_id = ?";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = "SELECT g.genre_id, g.name FROM genres g " + "JOIN films_genres fg ON g.genre_id = fg.genre_id " + "WHERE fg.film_id = ? order by g.genre_id";
    private static final String FIND_FILM_COUNT_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, " + "f.duration, f.mpa_id, m.name AS mpa_name, " + "COUNT(fl.user_id) AS likes_count " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " + "LEFT JOIN LIKES fl ON f.film_id = fl.film_id " + "GROUP BY f.film_id, m.name " + "ORDER BY likes_count DESC " + "LIMIT ?";
    private static final String FIND_FILMS_BY_DIRECTOR_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, " +
            "f.duration, f.mpa_id, m.name AS mpa_name, " +
            "COUNT(fl.user_id) AS likes_count " +
            "FROM films f " +
            "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
            "LEFT JOIN LIKES fl ON f.film_id = fl.film_id " +
            "JOIN films_directors fd ON f.film_id = fd.film_id " +
            "WHERE fd.director_id = ? " +
            "GROUP BY f.film_id, m.name " +
            "ORDER BY %s " +
            "LIMIT ?";
    private static final String FIND_DIRECTORS_BY_FILM_ID_QUERY = "SELECT d.director_id, d.name FROM directors d " +
            "JOIN films_directors fd ON d.director_id = fd.director_id " +
            "WHERE fd.film_id = ? ORDER BY d.director_id";

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    @Override
    public Film create(Film film) {
        Long mpaId = film.getMpa().getId();
        Mpa mpa = mpaService.getById(mpaId);

        Set<Genre> uniqueGenres = film.getGenres().stream().map(g -> genreService.findById(g.getId())).collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(uniqueGenres);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[]{"film_id"});
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
        if (!film.getGenres().isEmpty()) {
            saveGenres(newId, film.getGenres());
        }
        return findById(newId).orElseThrow(() -> new RuntimeException("Фильм не найден после добавления"));
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        int removed = jdbcTemplate.update(DELETE_QUERY, filmId, userId);
        if (removed > 0) {
            log.info("Лайк пользователя {} удалён у фильма {}", userId, filmId);
        } else {
            log.warn("Не найден лайк пользователя {} для фильма {}", userId, filmId);
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(INSERT_LIKES_QUERY, filmId, userId);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_QUERY, new FilmMapper());
        log.info("Найдено фильмов: {}", films.size());
        return films;
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        if (genres.size() > 0) {
            Genre[] g = genres.toArray(new Genre[genres.size()]);
            jdbcTemplate.batchUpdate(INSERT_FILMS_GENRES_QUERY, new BatchPreparedStatementSetter() {
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
        jdbcTemplate.update(DELETE_QUERY_FROM_FILMS_GENRES, filmId);
        saveGenres(filmId, genres);
    }

    @Override
    public Film update(Film film) {
        Film existingFilm = findById(film.getId()).orElseThrow(() -> new RuntimeException("Фильм с id " + film.getId() + " не найден"));

        Mpa mpa = mpaService.getById(film.getMpa().getId());
        film.setMpa(mpa);
        int rowsUpdated = jdbcTemplate.update(UPDATE_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), (int) film.getDuration().toMinutes(), mpa.getId(), film.getId());


        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с id=" + film.getId() + " не найден");
        }


        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateGenres(film.getId(), film.getGenres());
        }

        List<Genre> genreList = jdbcTemplate.query(FIND_GENRES_QUERY, (rs, rowNum) -> new Genre(rs.getLong("genre_id"), rs.getString("name")), film.getId());
        film.setGenres(new LinkedHashSet<>(genreList));

        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(FIND_FILMS_BY_JOIN_QUERY, new FilmMapper(), id);
            List<Genre> genres = jdbcTemplate.query(FIND_GENRES_QUERY, (rs, rn) -> new Genre(rs.getLong("genre_id"), rs.getString("name")), id);
            film.setGenres(new LinkedHashSet<>(genres));
            return Optional.of(film);

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return jdbcTemplate.query(FIND_FILM_COUNT_QUERY, new FilmMapper(), count);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy, int count) {
        String orderBy = "likes_count DESC";
        if ("year".equalsIgnoreCase(sortBy)) {
            orderBy = "release_date ASC";
        }
        String query = String.format(FIND_FILMS_BY_DIRECTOR_QUERY, orderBy);
        List<Film> films = jdbcTemplate.query(query, new FilmMapper(), directorId, count);
        for (Film film : films) {
            List<Genre> genres = jdbcTemplate.query(FIND_GENRES_BY_FILM_ID_QUERY, (rs, rn) -> new Genre(rs.getLong("genre_id"), rs.getString("name")), film.getId());
            film.setGenres(new LinkedHashSet<>(genres));
            List<Director> directors = jdbcTemplate.query(FIND_DIRECTORS_BY_FILM_ID_QUERY, (rs, rn) -> new Director(rs.getLong("director_id"), rs.getString("name")), film.getId());
            film.setDirectors(new LinkedHashSet<>(directors));
        }
        return films;
    }
}
