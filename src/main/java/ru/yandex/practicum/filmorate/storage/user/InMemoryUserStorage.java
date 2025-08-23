package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        boolean isUsed = users.entrySet().stream().anyMatch(entry -> entry.getValue().getEmail().equals(user.getEmail()));
        if (isUsed) {
            log.info("Этот имейл уже используется " + user.getEmail());
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

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.info("Id должен быть указан ");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Имени нет поставили логин");
            newUser.setName(newUser.getLogin());
        }
        if (users.containsKey(newUser.getId())) {
            users.put(newUser.getId(), newUser);
            log.info("Пользователь обновлен: {}", newUser);
            return newUser;
        }
        log.info("Пользователь с id = " + newUser.getId() + " не найден");
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void deleteById(Long userId) {
        if (users.remove(userId) != null) {
            log.info("Пользователь с id={} удален из InMemory хранилища", userId);
        } else {
            log.warn("Попытка удалить несуществующего пользователя с id={} из InMemory хранилища", userId);
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {

    }

    @Override
    public void removeFriend(Long userId, Long friendId) {

    }

    @Override
    public List<User> getFriends(Long userId) {
        return null;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return null;
    }
}
