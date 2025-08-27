package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.util.Arrays;
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


    /**
     * GET /films/popular
     * Возвращает список самых популярных фильмов с возможностью фильтрации по жанру и году
     *
     * @param count   количество возвращаемых фильмов (по умолчанию 10, min=1, max=2000)
     * @param genreId идентификатор жанра для фильтрации (опционально)
     * @param year    год выпуска для фильтрации (опционально, min=1895, max=2100)
     * @return список фильмов, отсортированный по убыванию количества лайков
     */
    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") @Min(1) @Max(2000) int count,
            @RequestParam(required = false) @Positive Long genreId,
            @RequestParam(required = false) @Min(1895) @Max(2100) Integer year) {

        log.debug("Запрос популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);

        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmOrThrow(id);
    }

    /**
     * Обрабатывает GET-запрос для получения списка общих с другом фильмов.
     * Эндпоинт: GET /films/common?userId={userId}&friendId={friendId}
     *
     * @param userId   идентификатор пользователя, переданный в параметрах запроса
     * @param friendId идентификатор друга, переданный в параметрах запроса
     * @return список общих фильмов, отсортированный по популярности (количеству лайков)
     */
    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam long userId,
                                     @RequestParam long friendId) {
        // Логируем факт поступления запроса
        log.info("Получен запрос GET /films/common с параметрами userId={}, friendId={}", userId, friendId);
        // Делегируем выполнение бизнес-логики сервисному слою и возвращаем результат
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long filmId) {
        log.info("Получен запрос DELETE /films/{}", filmId);
        filmService.deleteById(filmId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by) {

        List<String> searchBy = Arrays.asList(by.split(","));
        validateSearchParameters(searchBy);

        return filmService.searchFilms(query, searchBy);
    }

    private void validateSearchParameters(List<String> searchBy) {
        for (String param : searchBy) {
            if (!param.equals("title") && !param.equals("director")) {
                throw new IllegalArgumentException("Parameter 'by' can only contain 'title' and/or 'director'");
            }
        }
    }


    @GetMapping("/director/{directorId}")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getFilmsByDirector(@PathVariable Long directorId,
                                         @RequestParam(defaultValue = "likes") String sortBy) {
        log.info("Fetching films for directorId={} with sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

}
