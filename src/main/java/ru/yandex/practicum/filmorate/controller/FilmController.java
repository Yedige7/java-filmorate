package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film newFilm) {
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            films.put(newFilm.getId(), newFilm);
            log.info("Фильм обновлен: {}", newFilm);
            return newFilm;
        }
        log.warn("Id должен быть указан");
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }
}
