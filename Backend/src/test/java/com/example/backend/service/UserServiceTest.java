package com.example.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.entity.User;
import com.example.backend.core.auth.service.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser_WhenExternalIdDoesNotExist() {
        String email = "mamady@example.com";
        String fullName = "Mamady Mansare";
        String externalId = "jumpcloud-sub-12345";

        when(userRepository.findByExternalId(externalId)).thenReturn(Optional.empty());
        userService.syncWithIdp(email, fullName, externalId);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldUpdateUser_WhenNameOrEmailHasChangedOnIdp() {
        String externalId = "jumpcloud-sub-12345";
        User existingUser = new User();
        existingUser.setExternalId(externalId);
        existingUser.setEmail("mamady@example.com");
        existingUser.setFullName("Ancien Nom");

        when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(existingUser));
        userService.syncWithIdp("mamady@example.com", "Nouveau Nom", externalId);


        verify(userRepository, times(1)).save(existingUser);
        assertEquals("Nouveau Nom", existingUser.getFullName());
    }

    @Test
    void shouldNotCallSave_WhenUserExistsAndDataIsIdentical() {
        String externalId = "jumpcloud-sub-12345";
        String email = "mamady@example.com";
        String fullName = "Mamady Mansare";

        User existingUser = new User();
        existingUser.setExternalId(externalId);
        existingUser.setEmail(email);
        existingUser.setFullName(fullName);

        when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(existingUser));
        userService.syncWithIdp(email, fullName, externalId);

        verify(userRepository, never()).save(any(User.class));
    }
}