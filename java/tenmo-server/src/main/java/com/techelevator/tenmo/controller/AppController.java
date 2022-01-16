package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.InsufficientFundsException;
import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class AppController {

    @Autowired
    AccountDao accountDao;

    @Autowired
    UserDao userDao;

    @Autowired
    TransferDAO transferDAO;

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public Balance getBalance(Principal principal) {

        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);

        BigDecimal balance = accountDao.retrieveBalance(userId);

        Balance balanceObject = new Balance();
        balanceObject.setBalance(balance);

        return balanceObject;
    }

    @RequestMapping(path="/users", method = RequestMethod.GET)
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @RequestMapping(path="/accounts/{accountId}/user", method = RequestMethod.GET)
    public User getUserByAccountId(@PathVariable int accountId) {
        return accountDao.getUserByAccountId(accountId);
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public Transfer createTransfer(@RequestBody @Valid Transfer transfer) throws InsufficientFundsException {
        if(!areFundsAvailable(transfer)) {
            throw new InsufficientFundsException();
        }
        if(transfer.getTransferStatus() == 2) {
            transferFunds(transfer);
        }
        return transferDAO.createTransfer(transfer);
    }

    @RequestMapping(path="/transfers/{id}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int id) {
        return transferDAO.getTransferById(id);
    }

    @RequestMapping(path = "/transfers/status={id}", method = RequestMethod.GET)
    public List<Transfer> getTransfersByUser(Principal principal, @PathVariable("id") int statusId) {
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        int accountId = accountDao.getAccountIdByUserId(userId);
        return transferDAO.getTransfersByAccountIdAndStatus(accountId, statusId);
    }

    @RequestMapping(path = "/users/{userId}/accounts", method = RequestMethod.GET)
    public int getAccountIdByUserId(@PathVariable int userId) {
        return accountDao.getAccountIdByUserId(userId);
    }

    @RequestMapping(path = "/transfers/requests", method = RequestMethod.PUT)
    public String updateTransfer(@RequestBody @Valid Transfer transfer, Principal principal) throws InsufficientFundsException{
        if(!areFundsAvailable(transfer)) {
            throw new InsufficientFundsException();
        }
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        int accountId = accountDao.getAccountIdByUserId(userId);
        User receivingUser = userDao.findUserByAccountId(transfer.getPayeeAccount());
        if(transfer.getPayerAccount() == accountId && transfer.getTransferStatus() == 2){
            transferDAO.updateTransferStatus(transfer);
            transferFunds(transfer);
            return "\nYou approved the transfer request for $" + transfer.getTransferAmount() + " to " + receivingUser.getUsername();
        }else if(transfer.getPayerAccount() != accountId){
            return "\nYou can't approve your own requests...";
        }
        return "\nYou rejected the transfer request for $" + transfer.getTransferAmount() + " to " + receivingUser.getUsername();
    }

    private boolean areFundsAvailable(Transfer transfer){
        if (transfer.getTransferAmount().compareTo(accountDao.retrieveBalance(
                accountDao.getUserIdByAccountId(transfer.getPayerAccount()))) > 0) {
            return false;
        } else {
            return true;
        }
    }

    private void transferFunds(Transfer transfer){
        accountDao.addToBalance(transfer.getTransferAmount().multiply(new BigDecimal("-1")), transfer.getPayerAccount());
        accountDao.addToBalance(transfer.getTransferAmount(), transfer.getPayeeAccount());
    }
}
