package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Component
public class FilmWithDetailsMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(Duration.ofMinutes(rs.getInt("duration")));

        // MPA
        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        // Жанры
        String genreIdsStr = rs.getString("genre_ids");
        String genreNamesStr = rs.getString("genre_names");
        Set<Genre> genres = new HashSet<>();

        if (genreIdsStr != null && genreNamesStr != null) {
            String[] ids = genreIdsStr.split(",");
            String[] names = genreNamesStr.split(",");

            for (int i = 0; i < ids.length; i++) {
                if (!ids[i].trim().isEmpty()) {
                    Genre genre = new Genre();
                    genre.setId(Long.parseLong(ids[i].trim()));
                    genre.setName(names[i].trim());
                    genres.add(genre);
                }
            }
        }
        film.setGenres(genres);

        // Лайки
        String likesStr = rs.getString("user_likes");
        Set<Long> likes = new HashSet<>();
        if (likesStr != null) {
            String[] likeIds = likesStr.split(",");
            for (String id : likeIds) {
                if (!id.trim().isEmpty()) {
                    likes.add(Long.parseLong(id.trim()));
                }
            }
        }
        film.setLikes(likes);

        return film;
    }
}
