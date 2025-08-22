
SET REFERENTIAL_INTEGRITY FALSE;
DELETE FROM mpa;
DELETE FROM friends;
DELETE FROM films_genres;
DELETE FROM genres;
DELETE FROM likes;
DELETE FROM users;
DELETE FROM films;
DELETE FROM directors;
DELETE FROM films_directors;

ALTER TABLE users ALTER COLUMN USER_ID RESTART WITH 1;
ALTER TABLE films ALTER COLUMN FILM_ID RESTART WITH 1;
ALTER TABLE directors ALTER COLUMN DIRECTOR_ID RESTART WITH 1;

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

-- Заполнение таблицы режиссеров
INSERT INTO directors (director_id, name) VALUES
(1, 'Стенли Кубрик'),
(2, 'Квентин Тарантино'),
(3, 'Мартин Скорсезе'),
(4, 'Джордж Лукас'),
(5, 'Джеймс Кэмерон');

ALTER TABLE directors ALTER COLUMN director_id RESTART WITH 6;

-- Заполнение таблицы фильмов
INSERT INTO films (film_id, name, description, release_date, duration, mpa_id) VALUES
(1, 'Сияние', 'Писатель сходит с ума в уединенном отеле.', '1980-05-23', 144, 4),
(2, 'Космическая одиссея 2001 года', 'Эпическое путешествие к звездам.', '1968-04-03', 149, 1),
(3, 'Криминальное чтиво', 'Переплетенные истории гангстеров.', '1994-10-14', 154, 4),
(4, 'Убить Билла: Часть 1', 'Месть бывшей наемной убийцы.', '2003-10-10', 111, 4),
(5, 'Таксист', 'Ветеран Вьетнама борется с одиночеством.', '1976-02-08', 114, 4),
(6, 'Волк с Уолл-стрит', 'История взлета и падения биржевого брокера.', '2013-12-25', 180, 4),
(7, 'Звездные войны: Эпизод IV', 'Космическая сага о борьбе повстанцев.', '1977-05-25', 121, 2),
(8, 'Звездные войны: Эпизод V', 'Империя наносит ответный удар.', '1980-05-21', 124, 2),
(9, 'Терминатор 2: Судный день', 'Киборг защищает будущего лидера.', '1991-07-03', 137, 4),
(10, 'Аватар', 'Исследование инопланетного мира.', '2009-12-18', 162, 3);

-- Связь фильмов с режиссерами
INSERT INTO films_directors (film_id, director_id) VALUES
(1, 1), -- Сияние -> Стенли Кубрик
(2, 1), -- Космическая одиссея -> Стенли Кубрик
(3, 2), -- Криминальное чтиво -> Квентин Тарантино
(4, 2), -- Убить Билла -> Квентин Тарантино
(5, 3), -- Таксист -> Мартин Скорсезе
(6, 3), -- Волк с Уолл-стрит -> Мартин Скорсезе
(7, 4), -- Звездные войны IV -> Джордж Лукас
(8, 4), -- Звездные войны V -> Джордж Лукас
(9, 5), -- Терминатор 2 -> Джеймс Кэмерон
(10, 5); -- Аватар -> Джеймс Кэмерон

-- Связь фильмов и жанров
INSERT INTO films_genres (film_id, genre_id) VALUES
(1, 4), -- Сияние -> Триллер
(1, 2), -- Сияние -> Драма
(2, 6), -- Космическая одиссея -> Боевик
(3, 4), -- Криминальное чтиво -> Триллер
(3, 1), -- Криминальное чтиво -> Комедия
(4, 6), -- Убить Билла -> Боевик
(5, 2), -- Таксист -> Драма
(6, 2), -- Волк с Уолл-стрит -> Драма
(7, 6), -- Звездные войны IV -> Боевик
(8, 6), -- Звездные войны V -> Боевик
(9, 6), -- Терминатор 2 -> Боевик
(10, 6); -- Аватар -> Боевик

-- Добавление лайков
INSERT INTO likes (user_id, film_id) VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 3);

 */