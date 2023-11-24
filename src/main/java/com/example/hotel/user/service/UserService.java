package com.example.hotel.user.service;

import com.example.hotel.exception.InvalidDataFormatException;
import com.example.hotel.exception.EmailExistException;
import com.example.hotel.role.Role;
import com.example.hotel.role.RoleRepository;
import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.model.Guest;
import com.example.hotel.user.model.Host;
import com.example.hotel.user.model.User;
import com.example.hotel.user.repository.GuestRepository;
import com.example.hotel.user.repository.HostRepository;
import com.example.hotel.utils.ObjectsMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final HostRepository hostRepository;
    private final GuestRepository guestRepository;
    private final RoleRepository roleRepository;
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

    public void addRoleToGuest(String email, String roleName) {
        User user = guestRepository.findByEmail(email).get();
        Role role = roleRepository.findByName(roleName);
        if(role == null){
            role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
        user.getRoles().add(role);
    }

    public void addRoleToHost(String email, String roleName) {
        User user = hostRepository.findByEmail(email).get();
        Role role = roleRepository.findByName(roleName);
        user.getRoles().add(role);
    }

    public void registerUser(RegisterBodyDTO registerBodyDTO) throws EmailExistException, InvalidDataFormatException {
        System.out.println("darko");
        if (isAnyFieldEmpty(registerBodyDTO))
            throw new InvalidDataFormatException();
        if(registerBodyDTO.getRole().equals("Guest")) {
            if(guestRepository.findByEmail(registerBodyDTO.getEmail()).isPresent())
                throw new EmailExistException();
            if(hostRepository.findByEmail(registerBodyDTO.getEmail()).isPresent())
                throw new EmailExistException();
            Role role = roleRepository.findByName("ROLE_GUEST");
            System.out.println("marko");
            if (role == null) {
                role = new Role(0l, "ROLE_GUEST");
                roleRepository.save(role);
            }
            Guest guest = ObjectsMapper.convertRegisterDTOToGuest(registerBodyDTO);
            guest.setPassword(new BCryptPasswordEncoder().encode(registerBodyDTO.getPassword()));
            guestRepository.save(guest);
            addRoleToGuest(guest.getEmail(), role.getName());
        } else {
            if(hostRepository.findByEmail(registerBodyDTO.getEmail()).isPresent())
                throw new EmailExistException();
            if(guestRepository.findByEmail(registerBodyDTO.getEmail()).isPresent())
                throw new EmailExistException();
            Role role = roleRepository.findByName("ROLE_HOST");
            if (role == null) {
                role = new Role(0l, "ROLE_HOST");
                roleRepository.save(role);
            }
            Host host = ObjectsMapper.convertRegisterDTOToHost(registerBodyDTO);
            host.setPassword(new BCryptPasswordEncoder().encode(registerBodyDTO.getPassword()));
            hostRepository.save(host);
            addRoleToHost(host.getEmail(), role.getName());
        }
    }

    private boolean isAnyFieldEmpty(RegisterBodyDTO user) {
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
