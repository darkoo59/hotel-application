package com.example.hotel.user.service;

import com.example.hotel.exception.IncorrectPasswordException;
import com.example.hotel.exception.InvalidDataFormatException;
import com.example.hotel.exception.EmailExistException;
import com.example.hotel.exception.TokenNotValidException;
import com.example.hotel.role.Role;
import com.example.hotel.role.RoleRepository;
import com.example.hotel.user.dto.PasswordDTO;
import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.dto.UpdateUserDTO;
import com.example.hotel.user.model.*;
import com.example.hotel.user.repository.ConfirmationTokenRepository;
import com.example.hotel.user.repository.GuestRepository;
import com.example.hotel.user.repository.HostRepository;
import com.example.hotel.user.repository.PasswordResetTokenRepository;
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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
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

    public String registerUser(RegisterBodyDTO registerBodyDTO) throws EmailExistException, InvalidDataFormatException {
        if (isAnyFieldEmpty(registerBodyDTO))
            throw new InvalidDataFormatException();
        if(registerBodyDTO.getRole().equals("Guest")) {
            if(guestRepository.findByEmail(registerBodyDTO.getEmail()).isPresent())
                throw new EmailExistException();
            if(hostRepository.findByEmail(registerBodyDTO.getEmail()).isPresent())
                throw new EmailExistException();
            Role role = roleRepository.findByName("ROLE_GUEST");
            if (role == null) {
                role = new Role(0l, "ROLE_GUEST");
                roleRepository.save(role);
            }
            Guest guest = ObjectsMapper.convertRegisterDTOToGuest(registerBodyDTO);
            guest.setPassword(new BCryptPasswordEncoder().encode(registerBodyDTO.getPassword()));
            guestRepository.save(guest);
            addRoleToGuest(guest.getEmail(), role.getName());
            ConfirmationToken token = new ConfirmationToken(guest, UUID.randomUUID().toString());
            confirmationTokenRepository.save(token);
            return token.getToken();
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
            ConfirmationToken token = new ConfirmationToken(host, UUID.randomUUID().toString());
            confirmationTokenRepository.save(token);
            return token.getToken();
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

    public User getUserInfo(String username) {
        if(guestRepository.findByEmail(username).isPresent())
            return guestRepository.findByEmail(username).get();
        else if (hostRepository.findByEmail(username).isPresent())
            return hostRepository.findByEmail(username).get();
        throw new UsernameNotFoundException("User with provided username not found!");
    }

    public User updateUserInfo(UpdateUserDTO updateUserDTO, String username) throws EmailExistException {
        if(guestRepository.findByEmail(updateUserDTO.getEmail()).isPresent() && !updateUserDTO.getEmail().equals(username))
            throw new EmailExistException();
        if(hostRepository.findByEmail(updateUserDTO.getEmail()).isPresent() && ! updateUserDTO.getEmail().equals(username))
            throw new EmailExistException();
        User user;
        if(guestRepository.findByEmail(username).isPresent())
            user = guestRepository.findFirstByEmail(username);
        else if (hostRepository.findByEmail(username).isPresent())
            user = hostRepository.findFirstByEmail(username);
        else
            throw new UsernameNotFoundException("User with provided username not found!");
        user = updateInfoFromDTO(user, updateUserDTO);
        return user;
    }

    private User updateInfoFromDTO(User user, UpdateUserDTO dto) {
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setAddress(dto.getAddress());
        user.setSex(dto.getSex());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setBirthdate(dto.getBirthdate());
        return user;
    }

    public User findByEmail(String userEmail) {
        if(guestRepository.findByEmail(userEmail).isPresent())
            return guestRepository.findByEmail(userEmail).get();
        if(hostRepository.findByEmail(userEmail).isPresent())
            return hostRepository.findByEmail(userEmail).get();
        throw new UsernameNotFoundException("User with provided email not found!");
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    private void validatePasswordResetToken(String token, String email) throws TokenNotValidException {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findFirstByToken(token);
        if(passwordResetToken == null)
            throw new TokenNotValidException();
        if(!passwordResetToken.getUser().getEmail().equals(email))
            throw new TokenNotValidException();

    }

    public void changeUserPassword(PasswordDTO passwordDTO, String userEmail) throws TokenNotValidException, IncorrectPasswordException {
        validatePasswordResetToken(passwordDTO.getToken(), userEmail);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(guestRepository.findByEmail(userEmail).isPresent()){
            Guest guest = guestRepository.findByEmail(userEmail).get();
            if(!encoder.matches(passwordDTO.getOldPassword(), guest.getPassword()))
                throw new IncorrectPasswordException();
            guest.setPassword(new BCryptPasswordEncoder().encode(passwordDTO.getNewPassword()));
            guestRepository.save(guest);
            return;
        } else if (hostRepository.findByEmail(userEmail).isPresent()) {
            Host host = hostRepository.findByEmail(userEmail).get();
            if(!encoder.matches(passwordDTO.getOldPassword(), host.getPassword()))
                throw new IncorrectPasswordException();
            host.setPassword(new BCryptPasswordEncoder().encode(passwordDTO.getNewPassword()));
            hostRepository.save(host);
            return;
        }
           throw new UsernameNotFoundException("");
    }

    public void confirmRegistration(String token) throws TokenNotValidException {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findFirstByToken(token);
        if(confirmationToken == null)
            throw new TokenNotValidException();
        if(guestRepository.findByEmail(confirmationToken.getUser().getEmail()).isPresent()){
            Guest guest = guestRepository.findByEmail(confirmationToken.getUser().getEmail()).get();
            guest.setEnabled(true);
            guestRepository.save(guest);
            return;
        }else if(hostRepository.findByEmail(confirmationToken.getUser().getEmail()).isPresent()){
            Host host = hostRepository.findByEmail(confirmationToken.getUser().getEmail()).get();
            host.setEnabled(true);
            hostRepository.save(host);
            return;
        }
        throw new UsernameNotFoundException("User with that email not found");

    }
}
