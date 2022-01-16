package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal retrieveBalance(int userId);
    void addToBalance(BigDecimal amount, int accountId);
    int getUserIdByAccountId(int accountId);
    int getAccountIdByUserId(int userId);
    User getUserByAccountId(int accountId);
}
