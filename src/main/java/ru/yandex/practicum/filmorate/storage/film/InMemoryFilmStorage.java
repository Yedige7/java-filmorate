package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан: {}", film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            log.info("Id должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм обновлен: {}", film);
            return film;
        }
        log.info("Id должен быть указан");
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void deleteById(Long filmId) {
        if (films.remove(filmId) != null) {
            log.info("Фильм с id={} удален из InMemory хранилища", filmId);
        } else {
            log.warn("Попытка удалить несуществующий фильм с id={} из InMemory хранилища", filmId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {

    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return null;
    }

    @Override
    public void addLike(Long filmId, Long userId) {

    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        return List.of();
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        List<Film> filmsByDirector = films.values().stream()
                .filter(film -> film.getDirectors() != null &&
                        film.getDirectors().stream()
                                .anyMatch(director -> director.getId().equals(directorId)))
                .collect(Collectors.toList());

        if ("year".equalsIgnoreCase(sortBy)) {
            filmsByDirector.sort(Comparator.comparing(Film::getReleaseDate));
        } else {
            filmsByDirector.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));
        }

        return filmsByDirector;
    }
}
