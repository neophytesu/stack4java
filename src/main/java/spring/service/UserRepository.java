package spring.service;

import jdbc.*;
import jdbc.template.JdbcTemplate;
import mvc.dto.User;
import spring.di.annotation.Autowired;
import spring.service.annotations.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, _) -> new User(rs.getInt("id"), rs.getString("name"), rs.getInt("age"));

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM user", null, USER_ROW_MAPPER);
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM user WHERE id = ?", new Object[]{id}, USER_ROW_MAPPER));
    }

    public User insert(String name, int age) {
        int update = jdbcTemplate.update("INSERT INTO user (name, age) VALUES (?, ?)", name, age);
        if (update > 0) {
            return jdbcTemplate.queryForObject("SELECT * FROM user ORDER BY id DESC LIMIT 1", new Object[]{}, USER_ROW_MAPPER);
        }
        return null;
    }

    public boolean adjustAge(int id, int delta) {
        return jdbcTemplate.update("UPDATE user SET age = age + ? WHERE id = ?", delta, id) > 0;
    }

    public boolean update(int id, String name, Integer age) {
        return jdbcTemplate.update("UPDATE user SET name = ? ,age = ? WHERE id = ?", name, age, id) > 0;
    }

    public boolean deleteById(int id) {
        return jdbcTemplate.update("DELETE FROM user WHERE id = ?", id) > 0;
    }

}
