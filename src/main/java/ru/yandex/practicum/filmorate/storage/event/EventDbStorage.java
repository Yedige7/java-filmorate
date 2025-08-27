package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;
    private final EventMapper eventMapper;

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO events (ts, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().toString(),
                event.getOperation().toString(),
                event.getEntityId());
    }

    @Override
    public List<Event> getFeedForUser(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY ts";
        return jdbcTemplate.query(sql, eventMapper, userId);
    }
}
