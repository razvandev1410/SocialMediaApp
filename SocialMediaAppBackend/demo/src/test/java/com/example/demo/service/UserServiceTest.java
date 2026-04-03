package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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