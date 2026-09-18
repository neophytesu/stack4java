package jdbc.template;

import jdbc.*;
import jdbc.support.ConnectionUtils;

import java.util.ArrayList;
import java.util.List;


public class JdbcTemplate {
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, Object[] args, RowMapper<T> mapper) {
        Connection connection = getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = queryResult(ps, args)) {
            List<T> list = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                list.add(mapper.mapRow(rs, rowNum++));
            }
            return list;
        } finally {
            release(connection);
        }
    }

    private ResultSet queryResult(PreparedStatement ps, Object[] args) {
        bind(ps, args);
        return ps.executeQuery();
    }

    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> mapper) {
        List<T> list = query(sql, args, mapper);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new JdbcException("期望一行，实际 " + list.size() + " 行");
        }
        return list.getFirst();
    }

    public int update(String sql, Object... args) {
        Connection connection = getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, args);
            return ps.executeUpdate();
        } finally {
            release(connection);
        }
    }

    private Connection getConnection() {
        return ConnectionUtils.getConnection(dataSource);
    }

    private void release(Connection conn) {
        ConnectionUtils.releaseConnection(conn);
    }

    private void bind(PreparedStatement ps, Object[] args) {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                ps.setNull(i + 1);
            } else {
                ps.setObject(i + 1, arg);
            }
        }
    }
}
