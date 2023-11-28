package com.example.hotel.user.repository;

import com.example.hotel.user.model.ConfirmationToken;
import com.example.hotel.user.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, UUID> {
    ConfirmationToken findFirstByToken(String token);
}
