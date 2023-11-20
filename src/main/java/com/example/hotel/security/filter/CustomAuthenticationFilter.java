package com.example.hotel.security.filter;

import com.auth0.jwt.JWT;
import com.example.hotel.security.AuthUtility;
import com.example.hotel.user.dto.LoginBodyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.hotel.HotelApplication;
import java.util.Date;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws AuthenticationException {
        LoginBodyDTO loginDTO;
        try {
            loginDTO = new Gson().fromJson(request.getReader(), LoginBodyDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        User user = (User)authResult.getPrincipal();
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 1000 * 120))    // 1 hour
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().
                        map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(AuthUtility.getAlgorithm());
        String refreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000))    // 14 days
                .withIssuer(request.getRequestURL().toString())
                .sign(AuthUtility.getAlgorithm());
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        Cookie jwtCookie = new Cookie("refreshToken", refreshToken);
        jwtCookie.setMaxAge(14 * 24 * 60 * 60); // 14 days
//      jwtCookie.setSecure(true);     not using https
        jwtCookie.setHttpOnly(true);
        jwtCookie.setDomain("localhost");
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("errorMessage", "Incorrect email or password");
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }

}
