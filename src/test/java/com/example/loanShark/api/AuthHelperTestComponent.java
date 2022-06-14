package com.example.loanShark.api;

import com.example.loanShark.security.JwtTokenUtil;
import com.example.loanShark.security.models.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@TestComponent
public class AuthHelperTestComponent {

    @Autowired
    JwtTokenUtil jwtTokenProvider;

    public UserDetailsImpl getUserDetails(long userId, String username, String email, String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        return new UserDetailsImpl(userId, username, email, "1234", authorities);
    }

    public String getValidJtwToken(UserDetailsImpl userDetails) {
        var tokenString = jwtTokenProvider.generateToken(userDetails);
        return tokenString;
    }
}
