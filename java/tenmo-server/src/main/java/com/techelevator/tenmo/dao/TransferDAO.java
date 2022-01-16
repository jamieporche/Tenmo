package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDAO {

    public Transfer createTransfer(Transfer transfer);
    public List<Transfer> getTransfersByAccountIdAndStatus(int accountId, int statusId);
    public Transfer getTransferById(int id);
    public Transfer updateTransferStatus(Transfer transfer);

}
