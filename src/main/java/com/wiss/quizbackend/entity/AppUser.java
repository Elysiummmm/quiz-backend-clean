package com.wiss.quizbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(unique = true)
    @Size(max = 50)
    private String username;

    @NotNull
    @Column(unique = true)
    @Size(max = 100)
    private String email;

    @NotNull
    private String password;

    @NotNull
    private Role role;
}
