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

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public User create(User user) {
        if (emailExists(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rows = jdbcTemplate.update(
                sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday() != null ? Date.valueOf(newUser.getBirthday()) : null,
                newUser.getId()
        );


        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + newUser.getId() + " не найден");
        }

        return findById(newUser.getId()).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + newUser.getId() + " не найден после обновления"));
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE USER_ID = ?";

        return jdbcTemplate.query(sql, new UserMapper(), id)
                .stream()
                .findFirst();
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
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("friend_id"), id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.USER_ID = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, new UserMapper(), userId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }
}
