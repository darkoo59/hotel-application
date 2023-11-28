package com.example.hotel.user.dto;

import com.example.hotel.utils.enums.Sex;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserDTO {

    @Valid
    @NotNull(message = "Firstname may not be null")
    @NotBlank(message = "Firstname may not be blank")
    @NotEmpty(message = "Firstname may not be empty")
    private String firstname;
    @NotNull(message = "Lastname may not be null")
    @NotBlank(message = "Lastname may not be blank")
    @NotEmpty(message = "Lastname may not be empty")
    private String lastname;
    @Email(message = "Email isn't valid")
    @NotNull(message = "Email may not be null")
    @NotBlank(message = "Email may not be blank")
    @NotEmpty(message = "Email may not be empty")
    private String email;
    @NotNull(message = "Address may not be null")
    @NotBlank(message = "Address may not be blank")
    @NotEmpty(message = "Address may not be empty")
    private String address;
    @NotNull(message = "Phone may not be null")
    @NotBlank(message = "Phone may not be blank")
    @NotEmpty(message = "Phone may not be empty")
    private String phone;
    @NotNull(message = "Sex may not be null")
    private Sex sex;
    @NotNull(message = "Birthdate may not be null")
    private LocalDate birthdate;
}
