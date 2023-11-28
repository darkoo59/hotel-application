package com.example.hotel.user.repository;

import com.example.hotel.user.model.Host;
import com.example.hotel.user.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    PasswordResetToken findFirstByToken(String token);
}
