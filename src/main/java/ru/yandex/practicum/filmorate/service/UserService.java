package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final EventService eventService;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, EventService eventService) {
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }


    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        getUserOrThrow(newUser.getId());
        return userStorage.update(newUser);
    }

    public void addFriend(long userId, long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.addFriend(userId, friendId);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.FRIEND)
                .operation(Operation.ADD)
                .entityId(friendId)
                .build();
        eventService.addEvent(event);
    }

    public User getUserOrThrow(Long id) {
        return userStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь c " + id + " не найден"));
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.removeFriend(userId, friendId);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.FRIEND)
                .operation(Operation.REMOVE)
                .entityId(friendId)
                .build();
        eventService.addEvent(event);
    }

    public List<User> getFriends(Long userId) {
        getUserOrThrow(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        getUserOrThrow(userId);
        getUserOrThrow(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public void deleteById(Long userId) {
        getUserOrThrow(userId);
        userStorage.deleteById(userId);
        log.info("Пользователь с id={} удален", userId);
    }
}
