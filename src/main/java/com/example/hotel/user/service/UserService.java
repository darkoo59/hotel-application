package com.example.hotel.user.service;

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

    public void registerUser(RegisterBodyDTO registerBodyDTO) throws
            ObjectMappingException, EmailExistsException, InvalidDataFormatException {

        User user = ObjectsMapper.convertRegisterDTOToUser(registerBodyDTO);
        if (isAnyFieldEmpty(user)){
            throw new InvalidDataFormatException();
        }
        if(user.getRole().equals(Role.GUEST)) {
            Guest guest = (Guest)user;
            if (isAnyFieldEmpty(guest)){
                throw new InvalidDataFormatException();
            }
        }

        Client client;
        try {
            client = ObjectsMapper.convertRegisterBodyDTOToClient(registerBodyDTO);
        } catch (Exception e) {
            App.LOGGER.error(e.getMessage());
            throw new ObjectMappingException("user");
        }
        if (isAnyFieldEmpty(client)){
            throw new InvalidDataFormatException();
        }

        if (clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new EmailExistsException();
        }
        client.setRole(Role.CLIENT);
        saveClient(client);
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
