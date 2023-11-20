package com.example.hotel.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.hotel.HotelApplication;
import com.example.hotel.user.model.User;
import com.example.hotel.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletResponse;
import com.example.hotel.role.Role;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class AuthUtility {
    private final UserService userService;

    public AuthUtility(UserService userService) {
        this.userService = userService;
    }

    public String createJWTFromRequest(HttpServletRequest req) {
        String refreshToken = getRefreshToken(req);
        if (refreshToken == null) return null;
        DecodedJWT decodedJWT = getVerifier().verify(refreshToken);
        String email = decodedJWT.getSubject();
        User user = userService.getUserBy(email);
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 1000))    // 30 seconds
                .withIssuer(req.getRequestURL().toString())
                .withClaim("roles", user.getRoles().stream().
                        map(Role::getName).collect(Collectors.toList()))
                .sign(getAlgorithm());
    }

    public static String getEmailFromRequest(HttpServletRequest req) {
        String authorizationHeader = req.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String token = authorizationHeader.substring("Bearer ".length());
                DecodedJWT decodedJWT = getVerifier().verify(token);
                return decodedJWT.getSubject();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String getRefreshToken(HttpServletRequest req) {
        try {
            for (Cookie c : req.getCookies()) {
                if (c.getName().equals("refreshToken"))
                    return c.getValue();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static JWTVerifier getVerifier() {
        return JWT.require(getAlgorithm()).build();
    }

    public static Algorithm getAlgorithm() {
        return Algorithm.HMAC512("secret".getBytes());
    }

    public static void setResponseMessage(HttpServletResponse response, String messageName, String messageText)
            throws IOException {
        Map<String, String> responseObject = new HashMap<>();
        responseObject.put(messageName, messageText);
        new ObjectMapper().writeValue(response.getOutputStream(), responseObject);
    }
}
