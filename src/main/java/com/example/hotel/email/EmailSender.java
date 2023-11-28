package com.example.hotel.email;

import jakarta.mail.MessagingException;

public interface EmailSender {
    void send(String to, String subject, String email) throws MessagingException;
}
