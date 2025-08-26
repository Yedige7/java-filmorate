package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

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
    public List<Film> getCommonFilms(long userId, long friendId) {
        return List.of();
    }
}
