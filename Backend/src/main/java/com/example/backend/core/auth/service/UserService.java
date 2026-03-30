package com.example.backend.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.entity.User;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void syncWithIdp(String email, String fullName, String externalId) {

        userRepository.findByExternalId(externalId).ifPresentOrElse(

                existingUser -> {
                    if (!fullName.equals(existingUser.getFullName()) || !email.equals(existingUser.getEmail())) {
                        existingUser.setFullName(fullName);
                        existingUser.setEmail(email);
                        userRepository.save(existingUser);
                    }
                },

                () -> {
                    User newUser = new User();
                    newUser.setExternalId(externalId);
                    newUser.setEmail(email);
                    newUser.setFullName(fullName);
                    newUser.setActive(true);
                    userRepository.save(newUser);
                }
        );
    }
}