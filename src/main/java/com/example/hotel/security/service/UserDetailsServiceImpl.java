package com.example.hotel.security.service;

import com.example.hotel.exception.AccountNotConfirmedException;
import com.example.hotel.user.model.Guest;
import com.example.hotel.user.model.Host;
import com.example.hotel.user.repository.GuestRepository;
import com.example.hotel.user.repository.HostRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final GuestRepository guestRepository;
    private final HostRepository hostRepository;

    public UserDetailsServiceImpl(GuestRepository guestRepository, HostRepository hostRepository) {
        this.guestRepository = guestRepository;
        this.hostRepository = hostRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Guest guest = guestRepository.findFirstByEmail(username);
        if (guest != null) {
            if(!guest.isEnabled())
                throw new UsernameNotFoundException("User not found", null);
            return new org.springframework.security.core.userdetails.User(guest.getEmail(), guest.getPassword(), new ArrayList<>());
        }
        Host host = hostRepository.findFirstByEmail(username);
        if (host != null) {
            if(!host.isEnabled())
                throw new UsernameNotFoundException("User not found", null);
            return new org.springframework.security.core.userdetails.User(host.getEmail(), host.getPassword(), new ArrayList<>());
        }
        throw new UsernameNotFoundException("User not found", null);
    }
}
