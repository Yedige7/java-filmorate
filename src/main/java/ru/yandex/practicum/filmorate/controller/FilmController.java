package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
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
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getDuration() != null && !newFilm.getDuration().isNegative()) {
                oldFilm.setDuration(newFilm.getDuration());
            }

            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            log.info("Фильм обновлен: {}", oldFilm);
            return oldFilm;
        }
        log.warn("Id должен быть указан");
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @DeleteMapping("/clear")
    public void clearAll() {
        films.clear();
        log.trace("Все фильмы удалены");
    }
}
