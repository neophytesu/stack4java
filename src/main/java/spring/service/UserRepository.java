package spring.service;

import jdbc.Connection;
import jdbc.DataSource;
import jdbc.PreparedStatement;
import jdbc.ResultSet;
import jdbc.support.ConnectionUtils;
import mvc.dto.User;
import spring.di.annotation.Autowired;
import spring.service.annotations.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserRepository {
    @Autowired
    private DataSource dataSource;

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        Connection conn = ConnectionUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM user")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } finally {
            ConnectionUtils.releaseConnection(conn);
        }
        return users;
    }

    private User mapRow(ResultSet rs) {
        return new User(rs.getInt("id"), rs.getString("name"), rs.getInt("age"));
    }

    public Optional<User> findById(int id) {
        Connection conn = ConnectionUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } finally {
            ConnectionUtils.releaseConnection(conn);
        }
    }

    public User insert(String name, int age) {
        Connection conn = ConnectionUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO user (name, age) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setInt(2, age);
            int count = ps.executeUpdate();
            if (count > 0) {
                try (PreparedStatement p = conn.prepareStatement("SELECT * FROM user ORDER BY id DESC LIMIT 1");
                     ResultSet rs = p.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    return mapRow(rs);
                }
            }
        } finally {
            ConnectionUtils.releaseConnection(conn);
        }

        return null;
    }

    public boolean adjustAge(int id, int delta) {
        Connection conn = ConnectionUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("UPDATE user SET age = age + ? WHERE id = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } finally {
            ConnectionUtils.releaseConnection(conn);
        }
    }

    public boolean update(int id, String name, Integer age) {
        Connection conn = ConnectionUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("UPDATE user SET name = ? ,age = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setInt(3, id);
            int count = ps.executeUpdate();
            return count > 0;
        } finally {
            ConnectionUtils.releaseConnection(conn);
        }
    }

    public boolean deleteById(int id) {
        Connection conn = ConnectionUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id = ?")) {
            ps.setInt(1, id);
            int count = ps.executeUpdate();
            return count > 0;
        } finally {
            ConnectionUtils.releaseConnection(conn);
        }
    }

}
