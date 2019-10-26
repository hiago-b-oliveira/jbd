package io.jbd.weblogin.dao;

import io.jbd.weblogin.domain.UserAccount;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@CacheConfig(cacheNames = UserAccount.CACHE_NAME)
public class UserAccountDAO {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UserAccountDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Cacheable
    public UserAccount findByLogin(String login) {
        logger.info("findByLogin: {}", login);

        String sql = "select * from user_account ua where ua.login = ?";
        UserAccount userAccount = this.jdbcTemplate.query(sql, new Object[]{login}, getUserAccountResultSetExtractor());

        return userAccount;
    }

    private ResultSetExtractor<UserAccount> getUserAccountResultSetExtractor() {
        return rs -> (rs.next()) ? getUserAccountFromRS(rs) : null;
    }

    private UserAccount getUserAccountFromRS(ResultSet rs) throws SQLException {
        UserAccount ua = new UserAccount();
        ua.setId(rs.getLong("id"));
        ua.setLogin(rs.getString("login"));
        ua.setPassword(rs.getString("password"));
        ua.setName(rs.getString("name"));
        ua.setEmail(rs.getString("email"));
        ua.setRegisterDate(rs.getDate("register_date"));
        return ua;
    }
}
