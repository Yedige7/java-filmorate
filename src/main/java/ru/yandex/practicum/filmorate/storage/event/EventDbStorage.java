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
    private static final String SELECT_QUERY_BY_ID = "SELECT * FROM events WHERE user_id = ? ORDER BY ts";
    private static final String INSERT_QUERY = "INSERT INTO events (ts, user_id, event_type, operation, entity_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;
    private final EventMapper eventMapper;

    @Override
    public void addEvent(Event event) {
        jdbcTemplate.update(INSERT_QUERY,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().toString(),
                event.getOperation().toString(),
                event.getEntityId());
    }

    @Override
    public List<Event> getFeedForUser(Long userId) {
        return jdbcTemplate.query(SELECT_QUERY_BY_ID, eventMapper, userId);
    }
}
