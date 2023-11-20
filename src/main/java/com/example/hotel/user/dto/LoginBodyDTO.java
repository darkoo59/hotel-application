package com.example.hotel.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginBodyDTO {
    private String email;
    private String password;
}
