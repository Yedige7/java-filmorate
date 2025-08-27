package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;
    @Lazy
    private final UserService userService;

    public void addEvent(Event event) {
        eventStorage.addEvent(event);
    }

    public List<Event> getFeedForUser(Long userId) {
        userService.getUserOrThrow(userId);
        return eventStorage.getFeedForUser(userId);
    }
}