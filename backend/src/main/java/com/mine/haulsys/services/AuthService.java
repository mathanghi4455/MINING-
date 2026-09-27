package com.mine.haulsys.services;

import com.mine.haulsys.config.JwtTokenProvider;
import com.mine.haulsys.models.AppUser;
import com.mine.haulsys.dto.AuthResponse;
import com.mine.haulsys.dto.LoginRequest;
import com.mine.haulsys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String jwt = jwtTokenProvider.generateToken(authentication);
        AppUser user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new AuthResponse(jwt, user.getUsername(), user.getRole().name(), user.getFullName());
    }
}
