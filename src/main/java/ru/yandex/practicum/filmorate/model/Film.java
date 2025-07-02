package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.anotation.NotBefore;
import ru.yandex.practicum.filmorate.anotation.PositiveDuration;
import ru.yandex.practicum.filmorate.util.DurationFromMinutesDeserializer;
import ru.yandex.practicum.filmorate.util.DurationToMinutesSerializer;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
@EqualsAndHashCode(of = {"name"})
@AllArgsConstructor
@NoArgsConstructor
public class Film {

    private static final Logger log = LoggerFactory.getLogger(Film.class);

    Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    String description;

    @PositiveDuration
    @JsonSerialize(using = DurationToMinutesSerializer.class)
    @JsonDeserialize(using = DurationFromMinutesDeserializer.class)
    Duration duration;

    @NotBefore
    LocalDate releaseDate;

}
