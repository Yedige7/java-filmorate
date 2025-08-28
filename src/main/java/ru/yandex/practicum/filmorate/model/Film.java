package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.anotation.NotBefore;
import ru.yandex.practicum.filmorate.anotation.PositiveDuration;
import ru.yandex.practicum.filmorate.util.DurationFromMinutesDeserializer;
import ru.yandex.practicum.filmorate.util.DurationToMinutesSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@JsonIgnoreProperties(value = {"rate"})
@EqualsAndHashCode(of = {"name"})
@AllArgsConstructor
@NoArgsConstructor
public class  Film {

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

    private Mpa mpa;

    private Set<Genre> genres = new LinkedHashSet<>();

    @JsonProperty(value = "rate", access = JsonProperty.Access.READ_ONLY)
    public int getRate() {
        return likes != null ? likes.size() : 0;
    }

    private Set<Director> directors = new LinkedHashSet<>();

}
