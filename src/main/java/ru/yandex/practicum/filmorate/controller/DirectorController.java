package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable long id) {
        return directorService.findById(id);
    }

    @PostMapping
    public Director create(@RequestBody @Valid Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody @Valid Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        directorService.delete(id);
    }
}