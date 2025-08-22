package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.List;

@Service
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    @Autowired
    public DirectorService(DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public List<Director> findAll() {
        return directorDbStorage.findAll();
    }

    public Director findById(long id) {
        return directorDbStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }

    public Director create(Director director) {
        return directorDbStorage.create(director);
    }

    public Director update(Director director) {
        return directorDbStorage.update(director);
    }

    public void delete(long id) {
        directorDbStorage.delete(id);
    }
}