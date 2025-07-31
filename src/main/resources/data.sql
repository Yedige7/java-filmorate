--DELETE FROM FRIENDS;
--DELETE FROM FILMS;
--DELETE FROM USERS;
--DELETE FROM USERS;
--DELETE FROM FILMS_GENRE;
--DELETE FROM GENRE;
--DELETE FROM LIKES;
--DELETE FROM MPA;

INSERT INTO MPA (MPA_ID, NAME)
VALUES (1, 'G'),
VALUES (2, 'PG'),
VALUES (3, 'PG-13'),
VALUES (4, 'R'),
VALUES (5, 'NC-17');


INSERT INTO GENRE (GENRE_ID, NAME)
VALUES (1, 'Комедия'),
VALUES (2, 'Драма'),
VALUES (3, 'Мультфильм'),
VALUES (4, 'Триллер'),
VALUES (5, 'Документальный');
VALUES (5, 'Боевик');

/*
-- Фильмы

INSERT INTO "FILMS" ("NAME", "DESCRIPTION", "RELEASE_DATE", "DURATION", "MPA_ID")
VALUES ('Inception', 'Science fiction thriller by Christopher Nolan', '2010-07-16', INTERVAL '148 minutes', 1);

-- Пользователи
INSERT INTO "USERS" ("EMAIL", "LOGIN", "NAME", "BIRTHDAY")
VALUES ('Zhan.dos@example.com', 'ZhanDos', 'Zhan Dos', '1994-01-15');

INSERT INTO "USERS" ("EMAIL", "LOGIN", "NAME", "BIRTHDAY")
VALUES ('A.bay@example.com', 'Abay', 'Abay Bek', '1994-10-15');

-- Связь фильма и жанра
INSERT INTO "FILMS_GENRE" ("GENRE_ID", "FILM_ID")
VALUES (1, 1);

-- Дружба (неподтверждённая)
INSERT INTO "FRIENDS" ("USER_ID", "FRIEND_ID", "CONFIRMED")
VALUES (1, 2, false);

INSERT INTO "FRIENDS" ("USER_ID", "FRIEND_ID", "CONFIRMED")
VALUES (2, 1, true);

-- Лайк фильма пользователем
INSERT INTO "LIKES" ("USER_ID", "FILM_ID")
VALUES (1, 1);

 */