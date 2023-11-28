package com.example.hotel.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginBodyDTO {
    @Email(message = "Email isn't valid")
    @NotNull(message = "Email may not be null")
    @NotBlank(message = "Email may not be blank")
    @NotEmpty(message = "Email may not be empty")
    private String email;
    @NotNull(message = "Password may not be null")
    @NotBlank(message = "Password may not be blank")
    @NotEmpty(message = "Password may not be empty")
    private String password;
}
