package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class JdbcTransferDAOTest {
    public static SingleConnectionDataSource ds;
    private JdbcTransferDAO dao;
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
        this.dao = new JdbcTransferDAO(this.ds);
        loadData();
    }

    @After
    public void afterEach() throws SQLException {
        ds.getConnection().rollback();
    }

    @Test
    public void test_createTransfer() {
        Transfer transferToCreate = new Transfer(new BigDecimal("100.00"), 2, 2, 2001, 2002);
        Transfer createdTransfer = dao.createTransfer(transferToCreate);

        assertEquals(new BigDecimal("100.00"), createdTransfer.getTransferAmount());
        assertEquals(2, createdTransfer.getTransferType());
        assertEquals(2, createdTransfer.getTransferStatus());
        assertEquals(2002, createdTransfer.getPayerAccount());
        assertEquals(2001, createdTransfer.getPayeeAccount());
        assertTrue(createdTransfer.getTransferId() > 0);
    }

    @Test
    public void test_update_Transfer() {
        Transfer transferToCreate = new Transfer(new BigDecimal("100.00"), 1, 3, 2001, 2002);
        Transfer createdTransfer = dao.createTransfer(transferToCreate);
        int createdTransferId = createdTransfer.getTransferId();
        createdTransfer.setTransferStatus(2);
        dao.updateTransferStatus(createdTransfer);
        assertEquals(2, dao.getTransferById(createdTransferId).getTransferStatus());
    }

    @Test
    public void test_getTransferById_returns_null_when_not_found() {
        assertNull(dao.getTransferById(9999999));
    }

    @Test
    public void test_getTransfersByAccountId() {
        JdbcTemplate userJdbcTemplate = new JdbcTemplate(this.ds);
        UserDao userDao = new JdbcUserDao(userJdbcTemplate);
        JdbcTemplate accountJdbcTemplate = new JdbcTemplate(this.ds);
        AccountDao accountDao = new JdbcAccountDao(accountJdbcTemplate);
        User simonsUser = userDao.findByUsername("simon");
        User jeremysUser = userDao.findByUsername("jeremy");
        int simonsAccountId = accountDao.getAccountIdByUserId(simonsUser.getId().intValue());
        int jeremysAccountId = accountDao.getAccountIdByUserId(jeremysUser.getId().intValue());
        Transfer createdTransfer = new Transfer(new BigDecimal("10.00"), 2, 2, simonsAccountId, jeremysAccountId);
        Transfer createdTransfer2 = new Transfer(new BigDecimal("20.00"), 1, 1, simonsAccountId, jeremysAccountId);
        Transfer createdTransfer3 = new Transfer(new BigDecimal("30.00"), 2, 2, jeremysAccountId, simonsAccountId);
        Transfer createdTransfer4 = new Transfer(new BigDecimal("40.00"), 1, 1, jeremysAccountId, simonsAccountId);
        dao.createTransfer(createdTransfer);
        dao.createTransfer(createdTransfer2);
        dao.createTransfer(createdTransfer3);
        dao.createTransfer(createdTransfer4);

        List<Transfer> retrievedPendingTransfers = dao.getTransfersByAccountIdAndStatus(simonsAccountId, 1);
        List<Transfer> retrievedApprovedTransfers = dao.getTransfersByAccountIdAndStatus(simonsAccountId, 2);

        assertEquals(2, retrievedPendingTransfers.size());
        assertEquals(3, retrievedApprovedTransfers.size());
        assertEquals(simonsAccountId, retrievedPendingTransfers.get(0).getPayeeAccount());
        assertEquals(simonsAccountId, retrievedPendingTransfers.get(1).getPayerAccount());
    }

    public void loadData(){
        String sql1 = "INSERT INTO users(user_id, username, password_hash) VALUES (9001, 'simon', '1234567890')";
        String sql4 = "INSERT INTO users(user_id, username, password_hash) VALUES (9002, 'jeremy', '123456778890')";
        String sql2 = "INSERT INTO accounts(account_id, user_id, balance) VALUES (7777, (SELECT user_id FROM users WHERE username = 'simon'), 1000)";
        String sql5 = "INSERT INTO accounts(account_id, user_id, balance) VALUES (8888, (SELECT user_id FROM users WHERE username = 'jeremy'), 1000)";
        String sql3 = "INSERT INTO transfers(transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) "+
                "VALUES (2001, 2, 2, " +
                "(SELECT account_id FROM accounts " +
                "JOIN users ON accounts.user_id = users.user_id " +
                "WHERE username = 'simon'), " +
                "(SELECT account_id FROM accounts " +
                "JOIN users ON accounts.user_id = users.user_id " +
                "WHERE username = 'jeremy'), 100)";
        jdbcTemplate.update(sql1);
        jdbcTemplate.update(sql2);
        jdbcTemplate.update(sql4);
        jdbcTemplate.update(sql5);
        jdbcTemplate.update(sql3);
    }

}