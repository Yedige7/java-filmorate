package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import static org.hamcrest.Matchers.hasSize;

import java.time.Duration;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Jackson с модулями для Duration и LocalDate
    @BeforeEach
    void setup() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc.perform(delete("/films/clear"))
                .andExpect(status().isOk());
    }


    @Test
    void createFilmIntegrationTest() throws Exception {
        Film film = new Film(null, "С легким паром", "Советский фильм", Duration.ofMinutes(120), LocalDate.of(1990, 7, 16));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("С легким паром"));
    }


    @Test
    void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {
        Film film = new Film(
                null,
                "",
                "Пустое имя",
                Duration.ofMinutes(90),
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateFilmSuccessfully() throws Exception {
        // Сначала создаём фильм
        Film film = new Film(
                null,
                "С легким паром",
                "Совесткий фильм",
                Duration.ofMinutes(100),
                LocalDate.of(2000, 1, 1)
        );

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);

        // Обновляем
        Film updated = new Film(
                createdFilm.getId(),
                "Крик",
                "Крик ужастик",
                Duration.ofMinutes(200),
                LocalDate.of(2020, 1, 1)
        );

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Крик"))
                .andExpect(jsonPath("$.description").value("Крик ужастик"));
    }

    @Test
    void shouldReturnAllFilms() throws Exception {
        Film film1 = new Film(null, "Крик", "Крик ужастик", Duration.ofMinutes(90), LocalDate.of(2001, 1, 1));
        Film film2 = new Film(null, "Крик2", "Крик ужастик 2 часть", Duration.ofMinutes(100), LocalDate.of(2002, 2, 2));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
