package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.FilmWithDetailsMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
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
    private static final String INSERT_FILMS_DIRECTORS_QUERY = "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_QUERY_FROM_FILMS_DIRECTORS = "DELETE FROM films_directors WHERE film_id = ?";
    private static final String FIND_DIRECTORS_BY_FILM_ID_QUERY = "SELECT d.director_id, d.name FROM directors d " + "JOIN films_directors fd ON d.director_id = fd.director_id " + "WHERE fd.film_id = ? ORDER BY d.director_id";
    private static final String SEARCH_FILMS_BY_TITLE_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
            "m.mpa_id, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
            "WHERE LOWER(f.name) LIKE LOWER(?) " +
            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC";
    private static final String SEARCH_FILMS_BY_DIRECTOR_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
            "m.mpa_id, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
            "JOIN films_directors fd ON f.film_id = fd.film_id " +
            "JOIN directors d ON fd.director_id = d.director_id " +
            "WHERE LOWER(d.name) LIKE LOWER(?) " +
            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC";

    private static final String SEARCH_FILMS_BY_TITLE_AND_DIRECTOR_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
            "m.mpa_id, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
            "LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.director_id " +
            "WHERE LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?) " +
            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC";

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final FilmWithDetailsMapper filmWithDetailsMapper;
    private final DirectorService directorService;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;


    @Override
    public Film create(Film film) {
        Long mpaId = film.getMpa().getId();
        Mpa mpa = mpaService.getById(mpaId);

        Set<Genre> uniqueGenres = film.getGenres().stream().map(g -> genreService.findById(g.getId())).collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(uniqueGenres);

        // проверка режиссеров
        Set<Director> uniqueDirectors = film.getDirectors().stream()
                .map(d -> {
                    if (d.getId() == null) {
                        throw new ValidationException("Режиссер должен иметь ID");
                    }
                    return directorService.findById(d.getId());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

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
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();
        if (!film.getGenres().isEmpty()) {
            saveGenres(newId, film.getGenres());
        }
        if (!film.getDirectors().isEmpty()) {
            saveDirectors(newId, film.getDirectors()); // вызов сохранения режиссеров
        }
        return findById(newId).orElseThrow(() -> new NotFoundException("Фильм не найден после добавления"));
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_QUERY, new FilmMapper());

        for (Film film : films) {
            // Загрузка жанров
            List<Genre> genres = jdbcTemplate.query(FIND_GENRES_QUERY,
                    (rs, rowNum) -> new Genre(rs.getLong("genre_id"), rs.getString("name")),
                    film.getId());
            film.setGenres(new LinkedHashSet<>(genres));

            // Загрузка режиссеров
            List<Director> directors = jdbcTemplate.query(FIND_DIRECTORS_BY_FILM_ID_QUERY,
                    (rs, rowNum) -> new Director(rs.getLong("director_id"), rs.getString("name")),
                    film.getId());
            film.setDirectors(new LinkedHashSet<>(directors));
            loadLikesForFilm(film);
        }

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
        Film existingFilm = findById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

        Mpa mpa = mpaService.getById(film.getMpa().getId());
        film.setMpa(mpa);
        int rowsUpdated = jdbcTemplate.update(UPDATE_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), (int) film.getDuration().toMinutes(), mpa.getId(), film.getId());


        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с id=" + film.getId() + " не найден");
        }


            // Обновляем жанры, если поле в запросе присутствует
        if (film.getGenres() != null) {
            updateGenres(film.getId(), film.getGenres());
        } else {
            // Если поле genres вообще отсутствует в JSON, можно очистить жанры
            updateGenres(film.getId(), new HashSet<>());
        }

            // Аналогично для режиссеров
        if (film.getDirectors() != null) {
            updateDirectors(film.getId(), film.getDirectors());
        } else {
            updateDirectors(film.getId(), new HashSet<>());
        }

        List<Genre> genreList = jdbcTemplate.query(FIND_GENRES_QUERY, (rs, rowNum) -> new Genre(rs.getLong("genre_id"), rs.getString("name")), film.getId());
        film.setGenres(new LinkedHashSet<>(genreList));

        List<Director> directorList = jdbcTemplate.query(FIND_DIRECTORS_BY_FILM_ID_QUERY, (rs, rowNum) -> new Director(rs.getLong("director_id"), rs.getString("name")), film.getId());
        film.setDirectors(new LinkedHashSet<>(directorList));

        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(FIND_FILMS_BY_JOIN_QUERY, new FilmMapper(), id);
            if (film != null) {
                loadGenresForFilm(film);
                loadDirectorsForFilm(film);
                loadLikesForFilm(film);
            }
            return Optional.ofNullable(film);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        try {
            log.debug("getPopularFilms called with count={}, genreId={}, year={}", count, genreId, year);

            StringBuilder sql = new StringBuilder("""
                    SELECT\s
                        f.film_id,\s
                        f.name,\s
                        f.description,\s
                        f.release_date,\s
                        f.duration,\s
                        f.mpa_id,
                        m.name as mpa_name,
                        COUNT(l.user_id) AS likes_count
                    FROM films f
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    LEFT JOIN mpa m ON f.mpa_id = m.mpa_id
                   \s""");

            List<Object> params = new ArrayList<>();


            if (genreId != null && genreId > 0) {
                sql.append(" INNER JOIN films_genres fg ON f.film_id = fg.film_id");
            }

            sql.append(" WHERE 1=1");

            // Фильтр по жанру
            if (genreId != null && genreId > 0) {
                sql.append(" AND fg.genre_id = ?");
                params.add(genreId);
            }

            // Фильтр по году
            if (year != null && year > 0) {
                sql.append(" AND EXTRACT(YEAR FROM f.release_date) = ?");
                params.add(year);
            }

            sql.append(" GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name");
            sql.append(" ORDER BY likes_count DESC, f.film_id ASC");


            sql.append(" LIMIT ?");
            params.add(count);

            log.debug("Executing SQL: {}", sql.toString());
            log.debug("Params: {}", params);

            List<Film> result = jdbcTemplate.query(sql.toString(), this::mapRowToFilm, params.toArray());

            for (Film film : result) {
                loadGenresForFilm(film);
                loadDirectorsForFilm(film);
            }

            log.debug("Successfully found {} films", result.size());
            return result;

        } catch (Exception e) {
            log.error("Database error details:", e);
            throw new RuntimeException("Ошибка базы данных: " + e.getMessage(), e);
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        java.sql.Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        long duration = rs.getLong("duration");
        if (!rs.wasNull()) {
            film.setDuration(Duration.ofMinutes(duration));
        }

        if (rs.getObject("mpa_id") != null) {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getLong("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }

        film.setLikes(new HashSet<>());
        film.setGenres(new LinkedHashSet<>());

        return film;
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        String sqlFindCommonIds = "SELECT film_id FROM likes WHERE user_id = ? " +
                "INTERSECT " +
                "SELECT film_id FROM likes WHERE user_id = ?";

        List<Long> filmIds = jdbcTemplate.queryForList(sqlFindCommonIds, Long.class, userId, friendId);

        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String sqlGetFilms = String.format(FIND_ALL_QUERY + " WHERE f.film_id IN (%s)", inSql);

        List<Film> commonFilms = jdbcTemplate.query(sqlGetFilms, new FilmMapper(), filmIds.toArray());

        // Обогащаем фильмы деталями
        for (Film film : commonFilms) {
            loadGenresForFilm(film);
            loadDirectorsForFilm(film);
            loadLikesForFilm(film);
        }
        // Сортируем в памяти
        commonFilms.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));

        return commonFilms;
    }

    public void deleteById(Long filmId) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private void saveDirectors(Long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        if (directors.size() > 0) {
            Director[] d = directors.toArray(new Director[directors.size()]);
            jdbcTemplate.batchUpdate(INSERT_FILMS_DIRECTORS_QUERY, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, filmId);
                    ps.setLong(2, d[i].getId());
                }

                public int getBatchSize() {
                    return directors.size();
                }
            });
        }
    }

    private void updateDirectors(Long filmId, Set<Director> directors) {
        jdbcTemplate.update(DELETE_QUERY_FROM_FILMS_DIRECTORS, filmId);
        saveDirectors(filmId, directors);
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String orderByClause;
        if ("year".equalsIgnoreCase(sortBy)) {
            orderByClause = "ORDER BY f.release_date ASC";
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            orderByClause = "ORDER BY COUNT(l.user_id) DESC";
        } else {
            log.warn("Invalid sortBy parameter: {}, defaulting to likes", sortBy);
            orderByClause = "ORDER BY COUNT(l.user_id) DESC";
        }

        String sqlQuery =
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                        "m.mpa_id, m.name AS mpa_name, " +
                        "GROUP_CONCAT(DISTINCT g.genre_id) AS genre_ids, " +
                        "GROUP_CONCAT(DISTINCT g.name) AS genre_names, " +
                        "GROUP_CONCAT(DISTINCT d.director_id) AS director_ids, " +
                        "GROUP_CONCAT(DISTINCT d.name) AS director_names, " +
                        "GROUP_CONCAT(DISTINCT l.user_id) AS user_likes " +
                        "FROM films f " +
                        "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                        "LEFT JOIN films_genres fg ON f.film_id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                        "LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
                        "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                        "LEFT JOIN likes l ON f.film_id = l.film_id " +
                        "WHERE fd.director_id = ? " +
                        "GROUP BY f.film_id, m.mpa_id, m.name " +
                        orderByClause;

        try {
            return jdbcTemplate.query(sqlQuery, filmWithDetailsMapper, directorId);
        } catch (Exception e) {
            log.error("Error fetching films for directorId {} with sortBy={}: {}",
                    directorId, sortBy, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch films by director", e);
        }
    }

    @Override
    public List<Film> searchFilms(String query, List<String> searchBy) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchPattern = "%" + query.toLowerCase() + "%";
        List<Film> films;

        if (searchBy.contains("title") && searchBy.contains("director")) {
            // Поиск и по названию, и по режиссеру
            films = jdbcTemplate.query(SEARCH_FILMS_BY_TITLE_AND_DIRECTOR_QUERY,
                    new FilmMapper(), searchPattern, searchPattern);
        } else if (searchBy.contains("director")) {
            // Поиск только по режиссеру
            films = jdbcTemplate.query(SEARCH_FILMS_BY_DIRECTOR_QUERY,
                    new FilmMapper(), searchPattern);
        } else if (searchBy.contains("title")) {
            // Поиск только по названию
            films = jdbcTemplate.query(SEARCH_FILMS_BY_TITLE_QUERY,
                    new FilmMapper(), searchPattern);
        } else {
            throw new IllegalArgumentException("Invalid search parameters: " + searchBy);
        }

        // Загружаем дополнительные данные для каждого фильма
        for (Film film : films) {
            // Загрузка жанров
            List<Genre> genres = jdbcTemplate.query(FIND_GENRES_BY_FILM_ID_QUERY,
                    (rs, rn) -> new Genre(rs.getLong("genre_id"), rs.getString("name")),
                    film.getId());
            film.setGenres(new LinkedHashSet<>(genres));

            // Загрузка режиссеров
            List<Director> directors = jdbcTemplate.query(FIND_DIRECTORS_BY_FILM_ID_QUERY,
                    (rs, rn) -> new Director(rs.getLong("director_id"), rs.getString("name")),
                    film.getId());
            film.setDirectors(new LinkedHashSet<>(directors));
        }

        log.info("Найдено фильмов по запросу '{}' с параметрами {}: {}", query, searchBy, films.size());
        return films;
    }

    private void loadGenresForFilm(Film film) {
        List<Genre> genres = jdbcTemplate.query(FIND_GENRES_QUERY,
                (rs, rn) -> new Genre(rs.getLong("genre_id"), rs.getString("name")),
                film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadDirectorsForFilm(Film film) {
        List<Director> directors = jdbcTemplate.query(FIND_DIRECTORS_BY_FILM_ID_QUERY,
                (rs, rn) -> new Director(rs.getLong("director_id"), rs.getString("name")),
                film.getId());
        film.setDirectors(new LinkedHashSet<>(directors));
    }

    private void loadLikesForFilm(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.setLikes(new HashSet<>(userIds));
    }

    @Override
    public boolean hasLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }
}
