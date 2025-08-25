package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final EventService eventService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService, EventService eventService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.eventService = eventService;
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

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film getFilmOrThrow(Long id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм c " + id + " не найден"));
    }

    /**
     * Возвращает список общих с другом фильмов с сортировкой по их популярности.
     * Проверяет валидность идентификаторов пользователей перед выполнением запроса к БД.
     *
     * @param userId   идентификатор пользователя, запрашивающего информацию
     * @param friendId идентификатор пользователя, с которым происходит сравнение
     * @return список общих фильмов (объектов Film), отсортированный по популярности
     * @throws ValidationException если идентификаторы пользователей совпадают
     */
    public List<Film> getCommonFilms(long userId, long friendId) {
        // Проверка: не совпадают ли идентификаторы пользователей
        if (userId == friendId) {
            log.warn("Запрос общих фильмов для одинаковых ID пользователей: {}", userId);
            throw new ValidationException("Идентификаторы пользователя и друга не должны совпадать.");
        }

        // Проверка: существуют ли оба пользователя в базе данных.
        userService.getUserOrThrow(userId);
        userService.getUserOrThrow(friendId);

        log.info("Поиск общих фильмов для пользователей с ID: {} и {}", userId, friendId);
        // Делегируем выполнение основного запроса слою хранилища
        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        log.info("Найдено {} общих фильмов для пользователей с ID: {} и {}", commonFilms.size(), userId, friendId);

        return commonFilms;
    }
}
