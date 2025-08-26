package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        Film film = new Film(null, "С легким паром", "Советский фильм", Duration.ofMinutes(120),
                LocalDate.of(1990, 7, 16), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), new HashSet<>());

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
                new HashSet<>(),
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
                "Советский фильм",
                Duration.ofMinutes(100),
                LocalDate.of(2000, 1, 1),
                new HashSet<>(),
                new Mpa(1L, null),
                new HashSet<>(),
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
                new HashSet<>(),
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
        Film film1 = new Film(null, "Крик", "Крик ужастик", Duration.ofMinutes(90),
                LocalDate.of(2001, 1, 1), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), new HashSet<>());
        Film film2 = new Film(null, "Крик2", "Крик ужастик 2 часть", Duration.ofMinutes(100),
                LocalDate.of(2002, 2, 2), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), new HashSet<>());

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
        Film film1 = new Film(null, "Крик", "Крик ужастик", Duration.ofMinutes(90),
                LocalDate.of(2001, 1, 1), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), new HashSet<>());
        User u1 = new User(null, "user1@example.com", "login1", "User1",
                LocalDate.of(1990, 1, 1), new HashSet<>());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Крик"));

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"name\":\"Крик\",\"description\":\"Крик ужастик\",\"releaseDate\":\"2001-01-01\",\"duration\":90,\"likes\":[],\"genres\":[],\"directors\":[]}]"));
    }

    @Test
    void shouldReturnFilmsByDirectorSortedByLikes() throws Exception {
        // Создаем режиссера
        Director directorRequest = new Director(null, "Christopher Nolan");
        String directorResponse = mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(directorRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Director createdDirector = objectMapper.readValue(directorResponse, Director.class);

        User user = new User(null, "user@example.com", "userlogin", "User",
                LocalDate.of(1990, 1, 1), new HashSet<>());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        Film film1 = new Film(null, "Inception", "Dream infiltration", Duration.ofMinutes(148),
                LocalDate.of(2010, 7, 16), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), Set.of(createdDirector));

        String film1Response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Film createdFilm1 = objectMapper.readValue(film1Response, Film.class);

        Film film2 = new Film(null, "The Dark Knight", "Batman vs Joker", Duration.ofMinutes(152),
                LocalDate.of(2008, 7, 18), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), Set.of(createdDirector));

        String film2Response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Film createdFilm2 = objectMapper.readValue(film2Response, Film.class);

        mockMvc.perform(put("/films/" + createdFilm2.getId() + "/like/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/director/" + createdDirector.getId() + "?sortBy=likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(createdFilm2.getId()))
                .andExpect(jsonPath("$[0].releaseDate").value("2008-07-18"))
                .andExpect(jsonPath("$[0].directors[0].id").value(createdDirector.getId()))
                .andExpect(jsonPath("$[1].id").value(createdFilm1.getId()))
                .andExpect(jsonPath("$[1].releaseDate").value("2010-07-16"))
                .andExpect(jsonPath("$[1].directors[0].id").value(createdDirector.getId()));
    }

    @Test
    void shouldReturnFilmsByDirectorSortedByYear() throws Exception {
        Director directorRequest = new Director(null, "Christopher Nolan");
        String directorResponse = mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(directorRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Director createdDirector = objectMapper.readValue(directorResponse, Director.class);

        Film film1 = new Film(null, "Inception", "Dream infiltration", Duration.ofMinutes(148),
                LocalDate.of(2010, 7, 16), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), Set.of(createdDirector));

        String film1Response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Film createdFilm1 = objectMapper.readValue(film1Response, Film.class);

        Film film2 = new Film(null, "The Dark Knight", "Batman vs Joker", Duration.ofMinutes(152),
                LocalDate.of(2008, 7, 18), new HashSet<>(), new Mpa(1L, null),
                new HashSet<>(), Set.of(createdDirector));

        String film2Response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Film createdFilm2 = objectMapper.readValue(film2Response, Film.class);

        mockMvc.perform(get("/films/director/" + createdDirector.getId() + "?sortBy=year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(createdFilm2.getId()))
                .andExpect(jsonPath("$[0].releaseDate").value("2008-07-18"))
                .andExpect(jsonPath("$[0].directors[0].id").value(createdDirector.getId()))
                .andExpect(jsonPath("$[1].id").value(createdFilm1.getId()))
                .andExpect(jsonPath("$[1].releaseDate").value("2010-07-16"))
                .andExpect(jsonPath("$[1].directors[0].id").value(createdDirector.getId()));
    }
}
