package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;


import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    //методы добавления, удаления и модификации объектов.

    public Film create(Film film);

    public Collection<Film> findAll();

    public Film update(Film film);

    Optional<Film> findById(Long id);
}
