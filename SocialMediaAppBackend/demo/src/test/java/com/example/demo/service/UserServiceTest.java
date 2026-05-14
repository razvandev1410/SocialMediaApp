// DEPRECATED AFTER ASSIGNMENT 1


package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import org.apache.catalina.UserDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserService userService;

    private User user;
    private User moderator;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPhoneNumber("+40123456789");
        user.setPassword("encodedPassword");
        user.setScore(0.0);
        user.setIsBanned(false);
        user.setIsModerator(false);

        moderator = new User();
        moderator.setUserId(99L);
        moderator.setUsername("admin");
        moderator.setEmail("admin@test.com");
        moderator.setPassword("encodedPassword");
        moderator.setScore(0.0);
        moderator.setIsBanned(false);
        moderator.setIsModerator(true);
    }

    @Test
    void testBanUser_Success() {
        when(userRepository.findById(99L)).thenReturn(Optional.of(moderator));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.banUser(1L, 99L);

        assertTrue(user.getIsBanned());
        assertNotNull(result);
    }

    @Test
    void testBanUser_NotModerator_Throws() {
        User normalUser = new User();
        normalUser.setUserId(3L);
        normalUser.setIsModerator(false);

        when(userRepository.findById(3L)).thenReturn(Optional.of(normalUser));

        assertThrows(IllegalArgumentException.class, () -> userService.banUser(1L, 3L));
    }

    @Test
    void testUnbanUser_Success() {
        user.setIsBanned(true);

        when(userRepository.findById(99L)).thenReturn(Optional.of(moderator));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.unbanUser(1L, 99L);

        assertFalse(user.getIsBanned());
        assertNotNull(result);
    }

    @Test
    void testUnbanUser_NotModerator_Throws() {
        User normalUser = new User();
        normalUser.setUserId(3L);
        normalUser.setIsModerator(false);

        when(userRepository.findById(3L)).thenReturn(Optional.of(normalUser));

        assertThrows(IllegalArgumentException.class, () -> userService.unbanUser(1L, 3L));
    }

    @Test
    void testUpdateScore_AddsPoints() {
        user.setScore(5.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateScore(1L, 2.5);

        assertEquals(7.5, user.getScore());
    }

    @Test
    void testUpdateScore_SubtractsPoints() {
        user.setScore(3.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateScore(1L, -1.5);

        assertEquals(1.5, user.getScore());
    }

    @Test
    void testUpdateScore_CanGoNegative() {
        user.setScore(0.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateScore(1L, -1.5);

        assertEquals(-1.5, user.getScore());
    }

    // ========== CRUD ==========

    @Test
    void testRetrieveUsers() {
        User user2 = new User();
        user2.setUserId(2L);
        user2.setUsername("user2");
        user2.setEmail("u2@test.com");
        user2.setScore(0.0);
        user2.setIsBanned(false);
        user2.setIsModerator(false);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        List<UserDTO> result = userService.retrieveUsers();

        assertEquals(2, result.size());
    }

    @Test
    void testRetrieveUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.retrieveUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testRetrieveUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserDTO result = userService.retrieveUserById(999L);

        assertNull(result);
    }

    @Test
    void testFindEntityById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findEntityById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindEntityById_NotFound_Throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findEntityById(999L));
    }

    @Test
    void testInsertUser_EncodesPassword() {
        User newUser = new User();
        newUser.setUsername("new");
        newUser.setEmail("new@test.com");
        newUser.setPassword("plaintext");

        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setUserId(5L);
            return saved;
        });

        userService.insertUser(newUser);

        verify(passwordEncoder).encode("plaintext");
    }

    @Test
    void testInsertUser_DuplicateUsername_Throws() {
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("new@test.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.insertUser(newUser));
    }

    @Test
    void testDeleteById_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        String result = userService.deleteById(1L);

        assertEquals("User deletion successful", result);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound_Throws() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteById(999L));
    }



}

/*
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testRetrieveUsers() {
        User user1 = new User();
        user1.setUserId(1L);
        user1.setUsername("Andrei");

        User user2 = new User();
        user2.setUserId(2L);
        user2.setUsername("Alex");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> result = userService.retrieveUsers();

        assertEquals(2, result.size());
        assertEquals("Andrei", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testRetrieveUserById_Found() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("Andrei");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.retrieveUserById(1L);

        assertNotNull(result);
        assertEquals("Andrei", result.getUsername());
    }

    @Test
    void testRetrieveUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userService.retrieveUserById(999L);

        assertNull(result);
    }

    @Test
    void testInsertUser_ScoreDefaultsToZero() {
        User user = new User();
        user.setUsername("Andrei");
        user.setScore(null);

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.insertUser(user);

        assertEquals(0.0, user.getScore());
    }

    @Test
    void testInsertUser_ScoreKeptIfProvided() {
        User user = new User();
        user.setUsername("Andrei");
        user.setScore(5.0);

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.insertUser(user);

        assertEquals(5.0, user.getScore());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("Andrei123");

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(user);

        assertEquals("Andrei123", result.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteById_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        String result = userService.deleteById(1L);

        assertEquals("User deletion successful", result);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        String result = userService.deleteById(999L);

        assertEquals("User doesn't exist", result);
        verify(userRepository, never()).deleteById(999L);
    }
}
 */