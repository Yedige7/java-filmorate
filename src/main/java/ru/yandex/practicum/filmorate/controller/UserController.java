package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        boolean isUsed = users.entrySet().stream().anyMatch(entry -> entry.getValue().getEmail().equals(user.getEmail()));
        if (isUsed) {
            log.warn("Этот имейл уже используется " + user.getEmail());
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.trace("Id должен быть указан ");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
            }

            if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getBirthday() != null && !newUser.getBirthday().isAfter(LocalDate.now())) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            log.info("Пользователь обновлен: {}", oldUser);
            return oldUser;
        }
        log.warn("Пользователь с id = " + newUser.getId() + " не найден");
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @DeleteMapping("/clear")
    public void clearAll() {
        users.clear();
        log.trace("Все пользователи удалены");
    }

}
