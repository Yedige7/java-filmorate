
SET REFERENTIAL_INTEGRITY FALSE;
DELETE FROM mpa;
DELETE FROM friends;
DELETE FROM films_genres;
DELETE FROM genres;
DELETE FROM likes;
-- 2. Удаляем все данные
DELETE FROM users;
DELETE FROM films;
-- 3. Сбрасываем автоинкремент для id
ALTER TABLE users ALTER COLUMN USER_ID RESTART WITH 1;
ALTER TABLE films ALTER COLUMN FILM_ID RESTART WITH 1;

-- 4. Включаем проверки внешних ключей обратно
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO mpa (mpa_id, name)
VALUES (1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');


INSERT INTO genres (genre_id, name)
VALUES (1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

/*
-- Фильмы

INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Inception', 'Science fiction thriller by Christopher Nolan', '2010-07-16', INTERVAL '148 minutes', 1);

-- Пользователи
INSERT INTO users (email, login, name, birthday)
VALUES ('Zhan.dos@example.com', 'ZhanDos', 'Zhan Dos', '1994-01-15');

-- Пользователи
INSERT INTO users (email, login, name, birthday)
VALUES ('A.bay@example.com', 'Abay', 'Abay Bek', '1994-10-15');

-- Связь фильма и жанра
INSERT INTO films_genres (genre_id, film_id)
VALUES (1, 1);

-- Дружба (неподтверждённая)
INSERT INTO friends (user_id, friend_id, isConfirm)
VALUES (1, 2, false);

INSERT INTO friends (user_id, friend_id, isConfirm)
VALUES (2, 1, true);

-- Лайк фильма пользователем
INSERT INTO likes (user_id, film_id)
VALUES (1, 1);

 */