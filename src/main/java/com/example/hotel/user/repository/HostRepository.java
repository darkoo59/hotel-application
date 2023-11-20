package com.example.hotel.user.repository;

import com.example.hotel.user.model.Guest;
import com.example.hotel.user.model.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostRepository extends JpaRepository<Host, UUID> {
    Optional<Host> findByEmail(String email);
}
