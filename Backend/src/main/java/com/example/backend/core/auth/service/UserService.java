package com.example.backend.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.entity.User;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void syncWithIdp(String email, String fullName, String externalId) {

        userRepository.findByExternalId(externalId).ifPresentOrElse(
                // 1. SI L'UTILISATEUR EXISTE (Bloc Update)
                existingUser -> {
                    // On vérifie si JumpCloud a des données plus récentes
                    if (!fullName.equals(existingUser.getFullName()) || !email.equals(existingUser.getEmail())) {
                        existingUser.setFullName(fullName);
                        existingUser.setEmail(email);
                        userRepository.save(existingUser); // On ne sauvegarde QUE s'il y a un changement
                    }
                },
                // 2. SI L'UTILISATEUR N'EXISTE PAS (Bloc Create)
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