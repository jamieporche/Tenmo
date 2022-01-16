package com.techelevator.tenmo.model;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class BalanceTest {

    @Test
    public void test_that_balance_constructor_works_properly() {
        Balance testBalance = new Balance(new BigDecimal("10.00"));

        assertEquals(new BigDecimal("10.00"), testBalance.getBalance());
    }
}