package ru.yandex.practicum.filmorate.model.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private long eventId;
    private long timestamp;
    private long userId;
    private EventType eventType;
    private Operation operation;
    private long entityId;
}