package spring.service;

import jdbc.Connection;
import jdbc.DataSource;
import jdbc.PreparedStatement;
import jdbc.ResultSet;
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    private User mapRow(ResultSet rs) {
        return new User(rs.getInt("id"), rs.getString("name"), rs.getInt("age"));
    }

    public Optional<User> findById(int id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public User insert(String name, int age) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO user (name, age) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setInt(2, age);
            int count = ps.executeUpdate();
            if (count > 0) {
                try (Connection c = dataSource.getConnection()) {
                    PreparedStatement p = c.prepareStatement("SELECT * FROM user ORDER BY id DESC LIMIT 1");
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next()) {
                            return null;
                        }
                        return mapRow(rs);
                    }
                }
            }
        }

        return null;
    }

    public boolean update(int id, String name, Integer age) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE user SET name = ? ,age = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setInt(3, id);
            int count = ps.executeUpdate();
            return count > 0;
        }
    }

    public boolean deleteById(int id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id = ?")) {
            ps.setInt(1, id);
            int count = ps.executeUpdate();
            return count > 0;
        }
    }

}
