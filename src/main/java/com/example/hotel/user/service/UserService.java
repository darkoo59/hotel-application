package com.example.hotel.user.service;

import com.example.hotel.exception.InvalidDataFormatException;
import com.example.hotel.exception.EmailExistException;
import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.model.Guest;
import com.example.hotel.user.model.Host;
import com.example.hotel.user.model.User;
import com.example.hotel.user.repository.GuestRepository;
import com.example.hotel.user.repository.HostRepository;
import com.example.hotel.utils.ObjectsMapper;
import com.example.hotel.utils.enums.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final HostRepository hostRepository;
    private final GuestRepository guestRepository;
    public User getUserBy(String email) throws UsernameNotFoundException {
        Host host = hostRepository.findByEmail(email).orElse(null);
        if (host != null) {
            return host;
        }

        Guest guest = guestRepository.findByEmail(email).orElse(null);
        if (guest != null) {
            return guest;
        }
        throw new UsernameNotFoundException("user not found in the database");
    }

    public void registerUser(RegisterBodyDTO registerBodyDTO) throws EmailExistException, InvalidDataFormatException {

        User user = ObjectsMapper.convertRegisterDTOToUser(registerBodyDTO);
        if (isAnyFieldEmpty(user))
            throw new InvalidDataFormatException();
        if(user.getRole().equals(Role.GUEST)) {
            if(guestRepository.findByEmail(user.getEmail()).isPresent())
                throw new EmailExistException();
            Guest guest = (Guest)user;
            guest.setPassword(passwordEncoder.encode(guest.getPassword()));
            guestRepository.save(guest);
        } else {
            if(hostRepository.findByEmail(user.getEmail()).isPresent())
                throw new EmailExistException();
            Host host = (Host)user;
            host.setPassword(passwordEncoder.encode(host.getPassword()));
            hostRepository.save(host);
        }
    }

    private boolean isAnyFieldEmpty(User user) {
        return !StringUtils.hasLength(user.getFirstname()) ||
                !StringUtils.hasLength(user.getLastname()) ||
                !StringUtils.hasLength(user.getEmail()) ||
                !StringUtils.hasLength(user.getPassword()) ||
                !StringUtils.hasLength(user.getAddress()) ||
                user.getBirthdate() == null ||
                !StringUtils.hasLength(user.getPhone()) ||
                user.getSex() == null ||
                user.getRole() == null;
    }
}
