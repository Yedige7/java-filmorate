package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final EventService eventService;
    private final DirectorService directorService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService,
                       EventService eventService,
                       DirectorService directorService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.eventService = eventService;
        this.directorService = directorService;
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

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.ADD)
                .entityId(filmId)
                .build();
        eventService.addEvent(event);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        userService.getUserOrThrow(userId);

        if (!filmStorage.hasLike(filmId, userId)) {
            throw new NotFoundException(
                    String.format("Лайк от пользователя id=%d для фильма id=%d не найден.", userId, filmId)
            );
        }

        filmStorage.removeLike(filmId, userId);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.REMOVE)
                .entityId(filmId)
                .build();
        eventService.addEvent(event);
    }

    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        log.debug("Получение популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public Film getFilmOrThrow(Long id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм c " + id + " не найден"));
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        if (userId == friendId) {
            log.warn("Запрос общих фильмов для одинаковых ID пользователей: {}", userId);
            throw new ValidationException("Идентификаторы пользователя и друга не должны совпадать.");
        }

        userService.getUserOrThrow(userId);
        userService.getUserOrThrow(friendId);

        log.info("Поиск общих фильмов для пользователей с ID: {} и {}", userId, friendId);
        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        log.info("Найдено {} общих фильмов для пользователей с ID: {} и {}", commonFilms.size(), userId, friendId);

        return commonFilms;
    }

    public void deleteById(Long filmId) {
        getFilmOrThrow(filmId);
        filmStorage.deleteById(filmId);
        log.info("Фильм с id={} удален", filmId);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.findById(directorId); // проверка, что реж существует
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> searchFilms(String query, List<String> searchBy) {
        validateSearchParameters(searchBy);
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        return filmStorage.searchFilms(query.trim(), searchBy);
    }

    private void validateSearchParameters(List<String> searchBy) {
        for (String param : searchBy) {
            if (!param.equals("title") && !param.equals("director")) {
                throw new IllegalArgumentException("Parameter 'by' can only contain 'title' and/or 'director'");
            }
        }
    }
}
