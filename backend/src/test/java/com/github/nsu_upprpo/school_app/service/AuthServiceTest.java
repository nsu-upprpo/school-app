
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_returnsTokens_whenEmailIsFree() {
        RegisterRequest request = org.mockito.Mockito.mock(RegisterRequest.class);
        when(request.getFirstName()).thenReturn("Ирина");
        when(request.getLastName()).thenReturn("Родитель");
        when(request.getEmail()).thenReturn("new-parent@test.com");
        when(request.getPhone()).thenReturn("+79990000100");
        when(request.getPassword()).thenReturn("qwerty123");

        when(userRepository.existsByEmail("new-parent@test.com")).thenReturn(false);
        when(passwordEncoder.encode("qwerty123")).thenReturn("encoded-password");

        User savedUser = org.mockito.Mockito.mock(User.class);
        when(savedUser.getId()).thenReturn(SchoolSeedData.PARENT_ID);
        when(savedUser.getEmail()).thenReturn("new-parent@test.com");
        when(savedUser.getRole()).thenReturn(Role.PARENT);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtTokenProvider.generateAccessToken(SchoolSeedData.PARENT_ID, "new-parent@test.com", "PARENT"))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(SchoolSeedData.PARENT_ID, "new-parent@test.com", "PARENT"))
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3_600_000L);

        TokenResponse response = authService.register(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals("PARENT", response.getRole());
    }

    @Test
    void register_throwsConflictException_whenEmailAlreadyExists() {
        RegisterRequest request = org.mockito.Mockito.mock(RegisterRequest.class);
        when(request.getEmail()).thenReturn(SchoolSeedData.PARENT_EMAIL);
        when(userRepository.existsByEmail(SchoolSeedData.PARENT_EMAIL)).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void refresh_throwsBadRequestException_whenTokenTypeIsNotRefresh() {
        RefreshTokenRequest request = org.mockito.Mockito.mock(RefreshTokenRequest.class);
        when(request.getRefreshToken()).thenReturn("token");
        when(jwtTokenProvider.isValid("token")).thenReturn(true);
        when(jwtTokenProvider.getTokenType("token")).thenReturn("access");

        assertThrows(BadRequestException.class, () -> authService.refresh(request));
    }

    @Test
    void login_throwsBadCredentialsException_whenAuthenticationFails() {
        LoginRequest request = org.mockito.Mockito.mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("wrong@test.com");
        when(request.getPassword()).thenReturn("wrong");
        org.mockito.Mockito.doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_throwsBadRequestException_whenUserIsDeactivated() {
        LoginRequest request = org.mockito.Mockito.mock(LoginRequest.class);
        when(request.getEmail()).thenReturn(SchoolSeedData.PARENT_EMAIL);
        when(request.getPassword()).thenReturn("123456");

        User user = org.mockito.Mockito.mock(User.class);
        when(user.isActive()).thenReturn(false);
        when(userRepository.findByEmail(SchoolSeedData.PARENT_EMAIL)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }
}
