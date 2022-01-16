package com.techelevator.tenmo.model;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TransferTest {

    @Test
    public void test_that_constructor_works_properly() {
        Transfer testTransfer = new Transfer(new BigDecimal("10.00"), 2, 2,
                1001, 1002);

        assertEquals(new BigDecimal("10.00"), testTransfer.getTransferAmount());
        assertEquals(2, testTransfer.getTransferType());
        assertEquals(2, testTransfer.getTransferStatus());
        assertEquals(1001, testTransfer.getPayeeAccount());
        assertEquals(1002, testTransfer.getPayerAccount());
        assertEquals(0, testTransfer.getTransferId());
    }

}