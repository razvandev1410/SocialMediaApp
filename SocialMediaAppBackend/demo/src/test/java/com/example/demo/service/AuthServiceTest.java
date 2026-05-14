package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@test.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setPhoneNumber("+40123456789");
        existingUser.setScore(0.0);
        existingUser.setIsBanned(false);
        existingUser.setIsModerator(false);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setPhoneNumber("+40111222333");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setUserId(5L);
            return saved;
        });

        UserDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateUsername_Throws() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("new@test.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_DuplicateEmail_Throws() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("test@test.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_SetsDefaultValues() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@test.com");
        request.setPassword("pass");
        request.setPhoneNumber("+40111");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setUserId(5L);
            return saved;
        });

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                user.getScore() == 0.0 &&
                        !user.getIsBanned() &&
                        !user.getIsModerator()
        ));
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        UserDTO result = authService.login(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLogin_WrongPassword_Throws() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_BannedUser_Throws() {
        existingUser.setIsBanned(true);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_UserNotFound_Throws() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.login(request));
    }
}
