package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    public Film create(Film film);

    public Collection<Film> findAll();

    public Film update(Film film);

    Optional<Film> findById(Long id);

    void removeLike(Long filmId, Long userId);

    /**
     * Получает список популярных фильмов с возможностью фильтрации
     *
     * @param count   максимальное количество возвращаемых фильмов
     * @param genreId идентификатор жанра для фильтрации (может быть null)
     * @param year    год выпуска для фильтрации (может быть null)
     * @return список фильмов, отсортированный по убыванию популярности (количества лайков)
     * @throws IllegalArgumentException если передан неверный genreId или year
     */
    List<Film> getPopularFilms(int count, Long genreId, Integer year);


    void addLike(Long filmId, Long userId);

    /**
     * Получает список общих фильмов между двумя пользователями.
     *
     * @param userId   идентификатор первого пользователя
     * @param friendId идентификатор второго пользователя
     * @return список общих фильмов (объектов Film), отсортированный по популярности
     */
    List<Film> getCommonFilms(long userId, long friendId);

    void deleteById(Long filmId);

    List<Film> searchFilms(String query, List<String> searchBy);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);
}