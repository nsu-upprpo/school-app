package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.dto.request.LoginRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RefreshRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RegisterRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.TokenResponse;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.UserRepository;
import com.github.nsu_upprpo.school_app.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        return new TokenResponse();
    }

    public TokenResponse login(LoginRequest request) {
        return new TokenResponse();
    }

    public TokenResponse refresh(RefreshRequest request) {
        return new TokenResponse();
    }

    private TokenResponse generateToken(User user) {
        return new TokenResponse();
    }

}
