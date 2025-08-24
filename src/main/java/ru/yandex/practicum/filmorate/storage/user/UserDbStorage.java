package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT COUNT(*) FROM users WHERE email = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String FIND_BY_USER_ID_QUERY = "SELECT * FROM users WHERE USER_ID = ?";
    private static final String FIND_FRIEND_BY_USER_ID_QUERY = "SELECT friend_id FROM friends WHERE user_id = ?";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIEND_QUERY = "SELECT u.* FROM users u JOIN friends f ON u.USER_ID = f.friend_id " +
            "WHERE f.user_id = ?";
    private static final String FIND_COMMON_FRIEND_QUERY = "SELECT u.* FROM users u " +
            "JOIN friends f1 ON  f1.friend_id = u.user_id " +
            "JOIN friends f2 ON f2.friend_id = u.user_id " +
            "WHERE f1.user_id = ? AND " +
            "f2.user_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, (rs, rowNum) -> makeUser(rs));
    }

    public boolean emailExists(String email) {

        Integer count = jdbcTemplate.queryForObject(FIND_BY_EMAIL_QUERY, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public User create(User user) {
        if (emailExists(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            if (user.getName() == null || user.getName().isBlank()) {
                ps.setString(3, user.getLogin());
            } else {
                ps.setString(3, user.getName());
            }
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User newUser) {
        int rows = jdbcTemplate.update(
                UPDATE_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday() != null ? Date.valueOf(newUser.getBirthday()) : null,
                newUser.getId()
        );

        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + newUser.getId() + " не найден");
        }

        return newUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    FIND_BY_USER_ID_QUERY, new UserMapper(), id
            );
            return Optional.ofNullable(user);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private User makeUser(ResultSet rs) throws SQLException {
        Date date = rs.getDate("birthday");
        LocalDate birthday = (date != null) ? date.toLocalDate() : null;
        return new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                birthday,
                findFriendList(rs.getLong("user_id"))
        );
    }

    private Set<Long> findFriendList(Long id) {
        return new HashSet<>(jdbcTemplate.query(FIND_FRIEND_BY_USER_ID_QUERY, (rs, rowNum) -> rs.getLong("friend_id"), id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update(INSERT_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbcTemplate.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return jdbcTemplate.query(FIND_FRIEND_QUERY, new UserMapper(), userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return jdbcTemplate.query(FIND_COMMON_FRIEND_QUERY, new UserMapper(), userId, otherId);
    }
}
