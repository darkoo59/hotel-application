package com.example.hotel.user.model;

import com.example.hotel.role.Role;
import com.example.hotel.utils.enums.Sex;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Valid
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    private boolean enabled;
    private String firstname;
    private String lastname;
    private String email;
    @JsonIgnore
    private String password;
    private String address;
    private String phone;
    private Sex sex;
    private LocalDate birthdate;
    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    protected Collection<Role> roles = new ArrayList<>();

}
