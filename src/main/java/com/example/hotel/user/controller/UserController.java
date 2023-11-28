package com.example.hotel.user.controller;

import com.example.hotel.HotelApplication;
import com.example.hotel.email.EmailSender;
import com.example.hotel.exception.EmailExistException;
import com.example.hotel.exception.IncorrectPasswordException;
import com.example.hotel.exception.InvalidDataFormatException;
import com.example.hotel.exception.TokenNotValidException;
import com.example.hotel.security.service.UserDetailsServiceImpl;
import com.example.hotel.user.dto.LoginBodyDTO;
import com.example.hotel.user.dto.PasswordDTO;
import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.dto.UpdateUserDTO;
import com.example.hotel.user.model.User;
import com.example.hotel.user.service.UserService;
import com.example.hotel.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwt;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final EmailSender emailSender;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                          UserDetailsServiceImpl userDetailsService, EmailSender emailSender) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.emailSender = emailSender;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> Register(@RequestBody RegisterBodyDTO registerBodyDTO) {
        Map<String, String> responseObject = new HashMap<>();
        try {
            String token = userService.registerUser(registerBodyDTO);
            emailSender.send(registerBodyDTO.getEmail(), "Registration confirmation", "Registration confirmation link = " +
                    "https://localhost:8080/api/user/registrationConfirm?token="+token);
            return new ResponseEntity<>(CREATED);
        } catch (EmailExistException e) {
            responseObject.put("error", "email address is already taken");
            return new ResponseEntity<>(responseObject, CONFLICT);
        } catch (InvalidDataFormatException e) {
            responseObject.put("error", "invalid data format, please provide valid data");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        } catch (Exception e) {
            HotelApplication.LOGGER.error(e.getMessage());
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/token")
    public ResponseEntity<?> refreshToken(@RequestBody LoginBodyDTO loginDTO) throws IOException {
        Map<String, String> responseObject = new HashMap<>();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),
                    loginDTO.getPassword()));
            }catch (BadCredentialsException e) {
                responseObject.put("error", "Bad credentials provided");
                return new ResponseEntity<>(responseObject, UNAUTHORIZED);
            } catch (Exception e) {
                    responseObject.put("error", "Not succeed. Unknown error");
                    return new ResponseEntity<>(responseObject, INTERNAL_SERVER_ERROR);
            }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        responseObject.put("token", jwt);
        return new ResponseEntity<>(responseObject, CREATED);
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> userInfo(HttpServletRequest request) throws IOException {
        Map<String, String> responseObject = new HashMap<>();
        User user;
        try {
            String username = JwtUtil.getEmailFromRequest(request);
            user = userService.getUserInfo(username);
        }catch (UsernameNotFoundException e) {
            responseObject.put("error", "User with username not found");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        }catch (Exception e) {
            responseObject.put("error", "Not succeed. Unknown error");
            return new ResponseEntity<>(responseObject, INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<User>(user, CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<?> Register(@Valid @RequestBody UpdateUserDTO updateUserDTO, HttpServletRequest request) {
        Map<String, String> responseObject = new HashMap<>();
        try {
            User user = userService.updateUserInfo(updateUserDTO, JwtUtil.getEmailFromRequest(request));
            return new ResponseEntity<>(user, OK);
        } catch (EmailExistException e) {
            responseObject.put("error", "email address is already taken");
            return new ResponseEntity<>(responseObject, CONFLICT);
        } catch (UsernameNotFoundException e) {
            responseObject.put("error", "username isn't found");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        }catch (Exception e) {
            HotelApplication.LOGGER.error(e.getMessage());
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(HttpServletRequest request) {
        Map<String, String> responseObject = new HashMap<>();
        try {
            String userEmail = JwtUtil.getEmailFromRequest(request);
            User user = userService.findByEmail(userEmail);
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            emailSender.send(user.getEmail(), "Password Reset Token", "Your token = " + token);
            return new ResponseEntity<>("Mail with token is sent", OK);
        } catch (UsernameNotFoundException e) {
            responseObject.put("error", "email isn't found");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        } catch (MessagingException e) {
            responseObject.put("error", "Error while sending email");
            return new ResponseEntity<>(responseObject, INTERNAL_SERVER_ERROR);
        }catch (Exception e) {
            HotelApplication.LOGGER.error(String.valueOf(e.getCause()));
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> resetPassword(HttpServletRequest request,
                                           @Valid @RequestBody PasswordDTO passwordDTO) {
        Map<String, String> responseObject = new HashMap<>();
        try {
            String userEmail = JwtUtil.getEmailFromRequest(request);
            userService.changeUserPassword(passwordDTO, userEmail);
            return new ResponseEntity<>("Password successfully changed", OK);
        } catch (TokenNotValidException e) {
            responseObject.put("error", "token isn't found");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        }catch (IncorrectPasswordException e) {
            responseObject.put("error", "Provided old password is incorrect");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        }catch (Exception e) {
            HotelApplication.LOGGER.error(String.valueOf(e.getCause()));
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/registrationConfirm")
    public ResponseEntity<?> RegistrationConfirmation(@RequestParam("token") String token,HttpServletRequest request) throws IOException {
        Map<String, String> responseObject = new HashMap<>();
        try {
            userService.confirmRegistration(token);
            return new ResponseEntity<>(OK);
        }catch (UsernameNotFoundException e) {
            responseObject.put("error", "User with username not found");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        }catch (TokenNotValidException e) {
            responseObject.put("error", "Provided token is not valid");
            return new ResponseEntity<>(responseObject, BAD_REQUEST);
        }catch (Exception e) {
            responseObject.put("error", "Not succeed. Unknown error");
            return new ResponseEntity<>(responseObject, INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie blankCookie = new Cookie("refresh_token", "");
        blankCookie.setMaxAge(0);
        blankCookie.setHttpOnly(true);
        blankCookie.setDomain("localhost");
        blankCookie.setPath("/");
        response.addCookie(blankCookie);

        return new ResponseEntity<>(OK);
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return errors;
    }
}
