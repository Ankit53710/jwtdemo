package com.example.jwtdemo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.jwtdemo.DTO.JwtResponse;
import com.example.jwtdemo.DTO.LoginRequest;
import com.example.jwtdemo.DTO.SignupRequest;
import com.example.jwtdemo.Model.User;
import com.example.jwtdemo.Repository.UserRepository;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private AuthenticationManager authenticationManager;

    public void signup(SignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (userRepository.existsByEmail(user.getEmail())) {
          throw new RuntimeException("Email already exists.");
        }
        userRepository.save(user);
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String token = jwtUtils.generateToken(request.getEmail());
        return new JwtResponse(token, request.getEmail());
    }
}
