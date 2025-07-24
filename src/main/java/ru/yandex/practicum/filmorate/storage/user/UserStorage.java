package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    public Collection<User> findAll();

    public User create(User user);

    public User update(User newUser);

    Optional<User> findById(Long id);
}
