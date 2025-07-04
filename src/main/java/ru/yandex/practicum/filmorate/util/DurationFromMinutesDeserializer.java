package ru.yandex.practicum.filmorate.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

public class DurationFromMinutesDeserializer extends JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        long minutes = p.getLongValue();
        return Duration.ofMinutes(minutes);
    }
}
