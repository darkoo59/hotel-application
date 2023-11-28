package com.example.hotel.exception;

public class InvalidDataFormatException extends Exception {
    public InvalidDataFormatException() {
        super("Invalid data format, please provide valid data!");
    }
}
