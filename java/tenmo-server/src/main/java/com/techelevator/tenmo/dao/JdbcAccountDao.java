package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal retrieveBalance(int userId) {

        String sql = "SELECT balance FROM accounts WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        BigDecimal balance = null;

        if (results.next()) {
            balance = results.getBigDecimal("balance");
        }

        return balance;
    }

    public void addToBalance(BigDecimal amount, int accountId) {

        String sql = "UPDATE accounts " +
                "SET balance = balance + ? " +
                "WHERE account_id = ? ";
        jdbcTemplate.update(sql, amount, accountId);
    }

    public int getUserIdByAccountId(int accountId) {

        String sql = "SELECT user_id FROM accounts " +
                "WHERE account_id = ? ";
        Integer userId = jdbcTemplate.queryForObject(sql, Integer.class, accountId);
        return userId;
    }

    public User getUserByAccountId(int accountId) {

        String sql = "SELECT * FROM users " +
                "JOIN accounts ON users.user_id = accounts.user_id " +
                "WHERE account_id = ? ";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);

        if (results.next()) {
            return mapRowsToUser(results);
        }
        return null;
    }

    public int getAccountIdByUserId(int userId) {

        String sql = "SELECT account_id FROM accounts " +
                "WHERE user_id = ? ";
        Integer accountId = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return accountId;
    }

    private User mapRowsToUser(SqlRowSet rowSet) {
        User user = new User();
        user.setId(rowSet.getLong("user_id"));
        user.setUsername(rowSet.getString("username"));
        return user;
    }
}
