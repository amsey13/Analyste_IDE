package com.example.backend.api;

import com.example.backend.dto.UserProfileDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(
            @AuthenticationPrincipal OidcUser principal,
            HttpServletRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // --- LA CORRECTION ANTI-BLOCAGE FRONT-END ---
        // On récupère le jeton CSRF dans la requête et on appelle .getToken()
        // Cela force Spring à l'instancier et à l'envoyer dans le cookie "XSRF-TOKEN"
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