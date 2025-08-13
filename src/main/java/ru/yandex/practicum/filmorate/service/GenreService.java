package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Service
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public GenreService(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }


    public List<Genre> findAll() {
        return genreDbStorage.findAll();
    }


    public Genre findById(long id) {
        return genreDbStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Жанр c " + id + " не найден"));
    }
}
