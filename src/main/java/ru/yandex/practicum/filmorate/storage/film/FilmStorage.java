package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    public Film create(Film film);

    public Collection<Film> findAll();

    public Film update(Film film);

    Optional<Film> findById(Long id);

    void removeLike(Long filmId, Long userId);

    List<Film> getPopularFilms(int count);

    void addLike(Long filmId, Long userId);

    List<Film> getFilmsByDirector(Long directorId, String sortBy, int count);
}
