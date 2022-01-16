package com.techelevator.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Insufficient funds for transaction.")
public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 1L;

    public InsufficientFundsException() {
        super("Insufficient funds for transaction.");
    }
}


