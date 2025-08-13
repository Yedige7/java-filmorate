package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@Slf4j
@Service
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    @Autowired
    public MpaService(MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }


    public List<Mpa> findAll() {
        return mpaDbStorage.findAll();
    }


    public Mpa getById(long id) {
        log.info("id " + id);
        return mpaDbStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Рейтинг c " + id + " не найден"));
    }
}


