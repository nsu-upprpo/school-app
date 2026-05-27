package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.github.nsu_upprpo.school_app.common.exception.ConflictException;
import com.github.nsu_upprpo.school_app.model.dto.request.ChangePasswordRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.UpdateUserRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.UserResponse;
import com.github.nsu_upprpo.school_app.model.entity.Role;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getProfile_returnsParentProfile() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(SchoolSeedData.PARENT_ID);
        when(user.getFirstName()).thenReturn("Ирина");
        when(user.getLastName()).thenReturn("Родитель");
        when(user.getPatronymic()).thenReturn(null);
        when(user.getBirthDate()).thenReturn(null);
        when(user.getEmail()).thenReturn(SchoolSeedData.PARENT_EMAIL);
        when(user.getPhone()).thenReturn("+79990000004");
        when(user.getRole()).thenReturn(Role.PARENT);
        when(user.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 9, 1, 8, 0));

        when(userRepository.findById(SchoolSeedData.PARENT_ID)).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile(SchoolSeedData.PARENT_ID);

        assertEquals(SchoolSeedData.PARENT_ID, response.getId());
        assertEquals("Ирина", response.getFirstName());
        assertEquals("PARENT", response.getRole());
    }

    @Test
    void updateProfile_throwsConflict_whenNewEmailBusy() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getEmail()).thenReturn(SchoolSeedData.PARENT_EMAIL);

        UpdateUserRequest request = org.mockito.Mockito.mock(UpdateUserRequest.class);
        when(request.getFirstName()).thenReturn("Ирина");
        when(request.getLastName()).thenReturn("Родитель");
        when(request.getPhone()).thenReturn("+79990000004");
        when(request.getEmail()).thenReturn("busy@test.com");

        when(userRepository.findById(SchoolSeedData.PARENT_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("busy@test.com"))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(User.class)));

        assertThrows(
                ConflictException.class,
                () -> userService.updateProfile(SchoolSeedData.PARENT_ID, request)
        );
    }

    @Test
    void changePassword_updatesHash_whenCurrentPasswordIsValid() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getPasswordHash()).thenReturn("old-hash");

        when(userRepository.findById(SchoolSeedData.PARENT_ID)).thenReturn(Optional.of(user));

        ChangePasswordRequest request = org.mockito.Mockito.mock(ChangePasswordRequest.class);
        when(request.getCurrentPassword()).thenReturn("old");
        when(request.getNewPassword()).thenReturn("new-secret");
        when(passwordEncoder.matches("old", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-secret", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");

        userService.changePassword(SchoolSeedData.PARENT_ID, request);

        verify(user).setPasswordHash("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_throwsBadCredentials_whenNewPasswordEqualsCurrent() {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getPasswordHash()).thenReturn("old-hash");

        when(userRepository.findById(SchoolSeedData.PARENT_ID)).thenReturn(Optional.of(user));

        ChangePasswordRequest request = org.mockito.Mockito.mock(ChangePasswordRequest.class);
        when(request.getCurrentPassword()).thenReturn("same");
        when(request.getNewPassword()).thenReturn("same");
        when(passwordEncoder.matches("same", "old-hash")).thenReturn(true);

        assertThrows(
                BadCredentialsException.class,
                () -> userService.changePassword(SchoolSeedData.PARENT_ID, request)
        );
    }
}
