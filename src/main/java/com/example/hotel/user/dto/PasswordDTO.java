package com.example.hotel.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PasswordDTO {
    @Valid
    @NotNull(message = "Old password may not be null")
    @NotBlank(message = "Old password may not be blank")
    @NotEmpty(message = "Old password may not be empty")
    private String oldPassword;
    @NotNull(message = "Token may not be null")
    @NotBlank(message = "Token may not be blank")
    @NotEmpty(message = "Token may not be empty")
    private String token;
    @NotNull(message = "New password may not be null")
    @NotBlank(message = "New password may not be blank")
    @NotEmpty(message = "New password may not be empty")
    private String newPassword;
}
