package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import static org.hamcrest.Matchers.hasSize;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFilmIntegrationTest() throws Exception {
        Film film = new Film(null, "С легким паром", "Советский фильм", Duration.ofMinutes(120), LocalDate.of(1990, 7, 16), new HashSet<>(),  new Mpa(1L, null), new HashSet<>());

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
                LocalDate.of(2000, 1, 1),
                new HashSet<>(),
                new Mpa(1L, null),
                new HashSet<>()
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateFilmSuccessfully() throws Exception {

        Film film = new Film(
                null,
                "С легким паром",
                "Совесткий фильм",
                Duration.ofMinutes(100),
                LocalDate.of(2000, 1, 1),
                new HashSet<>(),
                new Mpa(1L, null),
                new HashSet<>()
        );

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);


        Film updated = new Film(
                createdFilm.getId(),
                "Крик",
                "Крик ужастик",
                Duration.ofMinutes(200),
                LocalDate.of(2020, 1, 1),
                new HashSet<>(),
                new Mpa(1L, null),
                new HashSet<>()
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
        Film film1 = new Film(null, "Крик", "Крик ужастик", Duration.ofMinutes(90), LocalDate.of(2001, 1, 1), new HashSet<>(),new Mpa(1L, null),  new HashSet<>());
        Film film2 = new Film(null, "Крик2", "Крик ужастик 2 часть", Duration.ofMinutes(100), LocalDate.of(2002, 2, 2), new HashSet<>(), new Mpa(1L, null),  new HashSet<>());

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

    @Test
    void testFilmLikesAndPopularity() throws Exception {
        Film film1 = new Film(null, "Крик", "Крик ужастик", Duration.ofMinutes(90), LocalDate.of(2001, 1, 1), new HashSet<>(), new Mpa(1L, null),  new HashSet<>());
        User u1 = new User(null, "user1@example.com", "login1", "User1", LocalDate.of(1990, 1, 1), new HashSet<>());

        // Создаем фильм
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        // Создаем пользователя
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        // PUT /films/{id}/like/{userId}
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());

        // GET /films/popular?count={count}
        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Крик"));

        // DELETE /films/{id}/like/{userId}
        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());

        // Проверяем, что лайков больше нет
        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"name\":\"Крик\",\"description\":\"Крик ужастик\",\"releaseDate\":\"2001-01-01\",\"duration\":90,\"likes\":[]}]"));
    }
}
