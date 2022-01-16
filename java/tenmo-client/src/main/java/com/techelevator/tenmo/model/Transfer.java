package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
    private BigDecimal transferAmount;
    private int transferType;
    private int transferStatus;
    private int payeeAccount;
    private int payerAccount;
    private int transferId;

    public Transfer(BigDecimal transferAmount, int transferType, int transferStatus, int payeeAccount, int payerAccount) {
        this.transferAmount = transferAmount;
        this.transferType = transferType;
        this.transferStatus = transferStatus;
        this.payeeAccount = payeeAccount;
        this.payerAccount = payerAccount;
    }

    public Transfer(){ }

    public int getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(int payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    public int getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(int payerAccount) {
        this.payerAccount = payerAccount;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal input){
        this.transferAmount = input;
    }

    public int getTransferType() { return transferType; }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public int getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(int transferStatus) {
        this.transferStatus = transferStatus;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }
}
