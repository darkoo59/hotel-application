package com.example.hotel.exception;

public class TokenNotValidException extends Exception{
    public TokenNotValidException() {
        super("Token not valid, please provide valid data!");
    }
}
