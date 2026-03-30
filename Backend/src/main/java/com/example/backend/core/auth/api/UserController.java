package com.example.backend.core.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.core.auth.dto.UserProfileDTO;

/**
 * REST Controller responsible for user-related authentication and profile management.
 * Provides endpoints to retrieve the security context and identity of the currently logged-in user.
*/

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Retrieves the profile details of the authenticated user.
     * * This endpoint performs three key actions:
     * Validates the existence of an OIDC session.
     * Force-loads the CSRF token from the request attributes to ensure it's 
     * included in the response (crucial for Single Page Applications).
     * Maps the OIDC principal attributes (email, name, subject) to a clean DTO.
     * * @param principal The authenticated OIDC user injected by Spring Security.
     * @param request   The current HTTP request used to access security attributes.
     * @return A {@link ResponseEntity} containing the {@link UserProfileDTO}, 
     * or a 401 Unauthorized status if no session exists.
    */

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(
            @AuthenticationPrincipal OidcUser principal,
            HttpServletRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        UserProfileDTO userProfile = new UserProfileDTO(
                principal.getEmail(),
                principal.getAttribute("name"),
                principal.getSubject()
        );

        return ResponseEntity.ok(userProfile);
    }
}

