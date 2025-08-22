package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final DirectorDbStorage directorDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService, DirectorDbStorage directorDbStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.directorDbStorage = directorDbStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        userService.getUserOrThrow(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        userService.getUserOrThrow(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film getFilmOrThrow(Long id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy, int count) {
        if (!directorDbStorage.findById(directorId).isPresent()) {
            return Collections.emptyList();
        }
        return filmStorage.getFilmsByDirector(directorId, sortBy, count);
    }
}
