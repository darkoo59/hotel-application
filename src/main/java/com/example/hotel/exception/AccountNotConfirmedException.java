package com.example.hotel.exception;

public class AccountNotConfirmedException extends Exception{
    public AccountNotConfirmedException() {
        super("Account not confirmed");
    }
}
