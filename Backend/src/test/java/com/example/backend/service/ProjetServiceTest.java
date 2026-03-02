package com.example.backend.service;

import com.example.backend.core.auth.dao.ProjetRepository;
import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.dto.ProjetDTO;
import com.example.backend.core.auth.exeption.UserNotFoundException;
import com.example.backend.core.auth.service.ProjetService;
import com.example.backend.core.entity.Projet;
import com.example.backend.core.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjetServiceTest {

    @Mock private ProjetRepository projetRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProjetService projetService;


    @Test
    public void testCreateProjetSuccess() throws UserNotFoundException {

        ProjetDTO test = new ProjetDTO();
        test.setNom("Test creation Projet");
        UUID id  = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(id);

        //on mock la securite statique

        try(MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {

            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            //simule les sortie des security elements de Spring
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("use-external-id");
            when(userRepository.findByExternalId("use-external-id")).thenReturn(Optional.of(mockUser));
            when(projetRepository.save(any(Projet.class))).thenAnswer(i -> i.getArguments()[0]);

            ProjetDTO projetResult = projetService.createProjet(test);

            assertNotNull(projetResult);
            assertEquals("Test creation Projet", projetResult.getNom());
            verify(projetRepository, times(1)).save(any(Projet.class));


        }
    }

    @Test
    public void testCreateProjectWithUserNotFoundException(){

        ProjetDTO test = new ProjetDTO();

        try(MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)){

            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("unknown-id");

            when(userRepository.findByExternalId("unknown-id")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> projetService.createProjet(test));



        }
    }


    @Test
    public void testGetProjetsFromUserSucces() {

        User mockUser = new User();
        UUID id = UUID.randomUUID();
        mockUser.setId(id);

        List<Projet> mockProjets = new ArrayList<>();
        Projet mockProjet1 = new Projet();
        mockProjet1.setNom("Projet 1");
        Projet mockProjet2 = new Projet();
        mockProjet2.setNom("Projet 2");
        mockProjets.add(mockProjet1);
        mockProjets.add(mockProjet2);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {

            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("user-external-id");

            when(userRepository.findByExternalId("user-external-id")).thenReturn(Optional.of(mockUser));
            when(projetRepository.findByUser(mockUser)).thenReturn(mockProjets);

            List<ProjetDTO> results = projetService.getProjetsFromUser();

            assertFalse(results.isEmpty());
            assertEquals(2, results.size());
        }


    }

    @Test
    public void testGetProjetsForEmptyList(){

        User mockUser = new User();

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("user-external-id");

            when(userRepository.findByExternalId("user-external-id")).thenReturn(Optional.of(mockUser));
            // On renvoie une liste vide au lieu d'une liste avec des projets
            when(projetRepository.findByUser(mockUser)).thenReturn(new ArrayList<>());

            List<ProjetDTO> result = projetService.getProjetsFromUser();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }





    }
}
