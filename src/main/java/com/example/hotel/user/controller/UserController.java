package com.example.hotel.user.controller;

import com.example.hotel.HotelApplication;
import com.example.hotel.exception.EmailExistException;
import com.example.hotel.exception.InvalidDataFormatException;
import com.example.hotel.security.service.UserDetailsServiceImpl;
import com.example.hotel.user.dto.LoginBodyDTO;
import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.service.UserService;
import com.example.hotel.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                          UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> Register(@RequestBody RegisterBodyDTO registerBodyDTO) {
        Map<String, String> responseObject = new HashMap<>();
        try {
            userService.registerUser(registerBodyDTO);
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

    @Secured({"ROLE_HOST"})
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return new ResponseEntity<>("Darko", OK);
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
}
