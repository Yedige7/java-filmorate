package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<Director> getAll() {
        List<Director> directors = directorStorage.findAll();
        log.info("Fetched {} directors: {}", directors.size(), directors);
        return directors;
    }

    public Optional<Director> findById(long id) {
        return directorStorage.findById(id);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        log.info("Processing update for director: {}", director);
        directorStorage.findById(director.getId())
                .orElseThrow(() -> {
                    log.warn("Director with id={} not found", director.getId());
                    return new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
                });
        Director updatedDirector = directorStorage.update(director);
        log.info("Successfully updated director: {}", updatedDirector);
        return updatedDirector;
    }

    public void delete(long id) {
        directorStorage.delete(id);
    }
}