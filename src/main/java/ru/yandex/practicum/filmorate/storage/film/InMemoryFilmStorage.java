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
    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        List<Film> result = new ArrayList<>();

        for (Film film : films.values()) {
            if (film != null) {
                result.add(film);
            }
        }

        Collections.sort(result, new Comparator<Film>() {
            public int compare(Film f1, Film f2) {
                int likes1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                int likes2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                return Integer.compare(likes2, likes1);
            }
        });

        if (count > 0 && count < result.size()) {
            return result.subList(0, count);
        }
        return result;
    }

    @Override
    public void addLike(Long filmId, Long userId) {

    }

    @Override
    public List<Film> searchFilms(String query, List<String> searchBy) {
        if (query == null || query.trim().isEmpty()) {
            log.info("Пустой запрос поиска");
            return Collections.emptyList();
        }

        String searchQuery = query.toLowerCase().trim();
        log.info("Поиск фильмов по запросу: '{}' с параметрами: {}", searchQuery, searchBy);

        boolean searchByTitle = searchBy.contains("title");
        boolean searchByDirector = searchBy.contains("director");

        if (!searchByTitle && !searchByDirector) {
            throw new IllegalArgumentException("Invalid search parameters: " + searchBy);
        }

        List<Film> foundFilms = films.values().stream()
                .filter(film -> {
                    boolean matches = false;
                    if (searchByTitle && film.getName() != null &&
                            film.getName().toLowerCase().contains(searchQuery)) {
                        matches = true;
                    }
                    if (!matches && searchByDirector && film.getDirectors() != null) {
                        matches = film.getDirectors().stream()
                                .anyMatch(director -> director.getName() != null &&
                                        director.getName().toLowerCase().contains(searchQuery));
                    }
                    return matches;
                })
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .collect(Collectors.toList());

        log.info("Найдено фильмов: {}", foundFilms.size());
        return foundFilms;
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
