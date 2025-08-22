package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import jakarta.validation.constraints.Min;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film newFilm) {
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmOrThrow(id);
    }

    @GetMapping("/directors/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable String directorId,
                                         @RequestParam(defaultValue = "likes") String sortBy,
                                         @RequestParam(defaultValue = "10") @Min(1) int count) {
        log.info("Fetching films for directorId: {}, sortBy: {}, count: {}", directorId, sortBy, count);
        try {
            long parsedDirectorId = Long.parseLong(directorId);
            if (parsedDirectorId < 1) {
                throw new ValidationException("ID режиссёра должен быть положительным");
            }

            if (!"likes".equalsIgnoreCase(sortBy) && !"year".equalsIgnoreCase(sortBy)) {
                throw new ValidationException("Параметр sortBy должен быть 'likes' или 'year'");
            }

            return filmService.getFilmsByDirector(parsedDirectorId, sortBy, count);
        } catch (NumberFormatException e) {
            log.warn("Invalid directorId format: {}", directorId);
            throw new ValidationException("Неверный формат ID режиссёра: " + directorId);
        }
    }
}