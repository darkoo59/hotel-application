package com.example.hotel.user.dto;

import com.example.hotel.utils.enums.Sex;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterBodyDTO {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String address;
    private String phone;
    private Sex sex;
    private String role;
    private LocalDate birthdate;
}
