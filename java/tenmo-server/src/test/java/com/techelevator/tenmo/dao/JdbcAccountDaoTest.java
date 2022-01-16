package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class JdbcAccountDaoTest {
    public static SingleConnectionDataSource ds;
    private JdbcAccountDao dao;
    private JdbcTemplate jdbcTemplate;

    @BeforeClass
    public static void beforeEverything() {
        ds = new SingleConnectionDataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/tenmo");
        ds.setUsername("postgres");
        ds.setPassword("postgres1");
        ds.setAutoCommit(false);
    }

    @AfterClass
    public static void afterEverything(){
        ds.destroy();
    }

    @Before
    public void beforeEach(){
        this.jdbcTemplate = new JdbcTemplate(this.ds);
        this.dao = new JdbcAccountDao(jdbcTemplate);
        loadData();
    }

    @After
    public void afterEach() throws SQLException {
        ds.getConnection().rollback();
    }

    @Test
    public void retrieve_balance_is_correct(){
        JdbcUserDao userDao = new JdbcUserDao(this.jdbcTemplate);
        int simonId = userDao.findIdByUsername("simon");
        assertEquals(new BigDecimal("700.00"), dao.retrieveBalance(simonId));
        int jeremyId = userDao.findIdByUsername("jeremy");
        assertEquals(new BigDecimal("100.00"), dao.retrieveBalance(jeremyId));
    }

    @Test
    public void get_account_by_user_and_update_balance_works(){
        JdbcUserDao userDao = new JdbcUserDao(this.jdbcTemplate);
        int simonId = userDao.findIdByUsername("simon");
        int jeremyId = userDao.findIdByUsername("jeremy");
        int simonAccount = dao.getAccountIdByUserId(simonId);
        int jeremyAccount = dao.getAccountIdByUserId(jeremyId);

        dao.addToBalance(new BigDecimal("220.00"), simonAccount);
        dao.addToBalance(new BigDecimal("-4400.00"), jeremyAccount);

        assertEquals(new BigDecimal("920.00"), dao.retrieveBalance(simonId));
        assertEquals(new BigDecimal("-4300.00"), dao.retrieveBalance(jeremyId));
    }

    @Test
    public void get_user_by_account_id_gives_null(){
        assertNull(dao.getUserByAccountId(567890));
    }

    @Test
    public void get_user_by_account_id_test(){
        JdbcUserDao userDao = new JdbcUserDao(this.jdbcTemplate);
        int simonId = userDao.findIdByUsername("simon");
        int jeremyId = userDao.findIdByUsername("jeremy");
        int simonAccount = dao.getAccountIdByUserId(simonId);
        int jeremyAccount = dao.getAccountIdByUserId(jeremyId);
        User simon = dao.getUserByAccountId(simonAccount);
        User jeremy = dao.getUserByAccountId(jeremyAccount);
        assertEquals("jeremy", jeremy.getUsername());
        assertEquals("simon", simon.getUsername());
        assertEquals(dao.getUserIdByAccountId(simonAccount), simon.getId().intValue());
        assertEquals(dao.getUserIdByAccountId(jeremyAccount), jeremy.getId().intValue());
    }

    public void loadData(){
        String sql1 = "INSERT INTO users(user_id, username, password_hash) VALUES (9001, 'simon', '1234567890')";
        String sql4 = "INSERT INTO users(user_id, username, password_hash) VALUES (9002, 'jeremy', '123456778890')";
        String sql2 = "INSERT INTO accounts(account_id, user_id, balance) VALUES (7777, (SELECT user_id FROM users WHERE username = 'simon'), 700)";
        String sql5 = "INSERT INTO accounts(account_id, user_id, balance) VALUES (8888, (SELECT user_id FROM users WHERE username = 'jeremy'), 100)";
        jdbcTemplate.update(sql1);
        jdbcTemplate.update(sql2);
        jdbcTemplate.update(sql4);
        jdbcTemplate.update(sql5);
    }
}