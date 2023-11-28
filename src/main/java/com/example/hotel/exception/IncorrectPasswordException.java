package com.example.hotel.exception;

public class IncorrectPasswordException extends Exception{
    public IncorrectPasswordException() {
        super("Provided password is incorrect!");
    }
}
