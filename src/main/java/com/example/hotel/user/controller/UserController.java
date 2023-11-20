package com.example.hotel.user.controller;

import com.example.hotel.HotelApplication;
import com.example.hotel.exception.EmailExistException;
import com.example.hotel.exception.InvalidDataFormatException;
import com.example.hotel.security.AuthUtility;
import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
    private final AuthUtility authUtility;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
        this.authUtility = new AuthUtility(this.userService);
    }

    @PostMapping("/register")
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

    @PostMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = authUtility.createJWTFromRequest(request);
            if (accessToken != null) {
                AuthUtility.setResponseMessage(response, "access_token", accessToken);
            } else {
                response.setStatus(UNAUTHORIZED.value());
                AuthUtility.setResponseMessage(response, "error", "refresh token is missing");
            }
        } catch (Exception e) {
            HotelApplication.LOGGER.error(e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR.value());
            AuthUtility.setResponseMessage(response, "error", "unknown error");
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
}
