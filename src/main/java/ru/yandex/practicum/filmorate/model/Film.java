package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.filmorate.anotation.NotBefore;
import ru.yandex.practicum.filmorate.anotation.PositiveDuration;
import ru.yandex.practicum.filmorate.util.DurationFromMinutesDeserializer;
import ru.yandex.practicum.filmorate.util.DurationToMinutesSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@EqualsAndHashCode(of = {"name"})
@AllArgsConstructor
@NoArgsConstructor
public class Film {

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String description;

    @PositiveDuration
    @JsonSerialize(using = DurationToMinutesSerializer.class)
    @JsonDeserialize(using = DurationFromMinutesDeserializer.class)
    private Duration duration;

    @NotBefore
    @NotNull
    private LocalDate releaseDate;

    @NotNull
    private Set<Long> likes = new HashSet<>();

}
