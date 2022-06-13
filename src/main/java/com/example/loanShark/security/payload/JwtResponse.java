package com.example.loanShark.security.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class JwtResponse {

    private String jwtToken;
    private Long id;
    private String username;
    private String email;
    List<String> roles;
}
