package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.BadRequestException;
import com.github.nsu_upprpo.school_app.common.exception.ConflictException;
import com.github.nsu_upprpo.school_app.model.dto.request.LoginRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RefreshTokenRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RegisterRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.TokenResponse;
import com.github.nsu_upprpo.school_app.model.entity.Role;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.UserRepository;
import com.github.nsu_upprpo.school_app.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected: email already exists [email={}]", request.getEmail());
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.PARENT)
                .build();
        user = userRepository.save(user);
        log.info("User registered [userId={}, email={}, role={}]", user.getId(), user.getEmail(), user.getRole());

        return generateToken(user);
    }

    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed: invalid credentials [email={}]", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: authenticated user not found in repository [email={}]", request.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });
        if (!user.isActive()) {
            log.warn("Login rejected: account deactivated [userId={}, email={}]", user.getId(), user.getEmail());
            throw new BadRequestException("Account deactivated");
        }
        log.info("User logged in [userId={}, role={}]", user.getId(), user.getRole());
        return generateToken(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.isValid(refreshToken)) {
            log.warn("Token refresh failed: invalid refresh token");
            throw new BadRequestException("Invalid refresh token");
        }
        if (!jwtTokenProvider.getTokenType(refreshToken).equals("refresh")) {
            log.warn("Token refresh failed: wrong token type supplied");
            throw new BadRequestException("Not a refresh token");
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Token refresh failed: user not found [email={}]", email);
            return new BadRequestException("User not found");
        });
        if (!user.isActive()) {
            log.warn("Token refresh rejected: account deactivated [userId={}]", user.getId());
            throw new BadRequestException("Account deactivated");
        }

        log.debug("Tokens refreshed [userId={}]", user.getId());
        return generateToken(user);
    }

    private TokenResponse generateToken(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getEmail(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
                .role(user.getRole().name())
                .build();
    }

}
