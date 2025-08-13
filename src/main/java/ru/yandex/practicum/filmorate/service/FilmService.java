package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film update(Film newFilm) {
        log.info("newFilm " + newFilm);
        return filmStorage.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        userService.getUserOrThrow(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info(" removeLike " + filmId + " " + userId);
        getFilmOrThrow(filmId);
        userService.getUserOrThrow(userId);
        filmStorage.removeLike(filmId, userId);

    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilmOrThrow(Long id) {
        return filmStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Фильм c " + id + " не найден"));
    }
}
