package mybatis.session;

import jdbc.template.JdbcTemplate;

public class SqlSessionFactory {
    private final JdbcTemplate jdbcTemplate;

    public SqlSessionFactory(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SqlSession openSession() {
        return new SqlSession(jdbcTemplate);
    }
}
