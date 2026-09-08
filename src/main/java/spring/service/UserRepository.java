package spring.service;

import mvc.dto.User;
import mysql.core.SqlEngine;
import mysql.core.SqlResult;
import mysql.storage.Row;
import spring.di.annotation.Autowired;
import spring.service.annotations.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserRepository {
    @Autowired
    private SqlEngine sqlEngine;

    public List<User> findAll() {
        SqlResult result = sqlEngine.execute("SELECT * FROM user");
        checkSuccess(result);
        return result.rows().stream().map(this::toUser).toList();
    }

    public Optional<User> findById(int id) {
        SqlResult result = sqlEngine.execute("SELECT * FROM user WHERE id = " + id);
        checkSuccess(result);
        if (result.rows().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toUser(result.rows().getFirst()));
    }

    public User insert(String name, int age) {
        SqlResult r = sqlEngine.execute("INSERT INTO user (name, age) VALUES ('" + escape(name) + "'," + age + ")");
        if (r.isSuccess()){
            SqlResult result = sqlEngine.execute("SELECT * FROM user ORDER BY id DESC LIMIT 1");
            return toUser(result.rows().getFirst());
        }
        return null;
    }

    public boolean update(int id, String name, Integer age) {
        SqlResult result = sqlEngine.execute("UPDATE user SET name = '" + escape(name) + "',age = " + age + " WHERE id = " + id);
        return result.isSuccess();
    }

    public boolean deleteById(int id) {
        SqlResult result = sqlEngine.execute("DELETE FROM user WHERE id = " + id);
        return result.isSuccess();
    }

    private User toUser(Row row) {
        Object[] v = row.getValues();
        return new User((Integer) v[0], (String) v[1], (Integer) v[2]);
    }

    private void checkSuccess(SqlResult result) {
        if (!result.isSuccess()) {
            throw new RuntimeException(result.executeResult().description());
        }
    }

    private String escape(String s) {
        return s.replace("'", "''");
    }
}
