package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }


    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        log.warn("1 newUser " + newUser);
        getUserOrThrow(newUser.getId());
        log.warn("2 newUser " + newUser);
        return userStorage.update(newUser);
    }

    public void addFriend(long userId, long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        userStorage.addFriend(userId, friendId); // сохраняем в БД
    }

    public User getUserOrThrow(Long id) {
        log.warn("getUserOrThrow " + id);
        return userStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь c " + id + " не найден"));
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        userStorage.removeFriend(userId, friendId); // удаляем в БД
    }

    public List<User> getFriends(Long userId) {
        getUserOrThrow(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        getUserOrThrow(userId);
        getUserOrThrow(otherId);
        Set<Long> userFriends = userStorage.getFriendIds(userId);
        Set<Long> otherFriends = userStorage.getFriendIds(otherId);

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }



}
