package com.example.backend.auth.config; // Modifie si ton package est différent

import com.example.backend.auth.exeption.SecurityConfigurationException;
import com.example.backend.auth.service.CustomOidcUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOidcUserService customOidcUserService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http
                    // 1. CORS : Autoriser les requêtes du Front-End
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                    // 2. CSRF : Sécuriser les requêtes d'altération de données (POST, PUT, DELETE)
                    .csrf(csrf -> csrf
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    )

                    // 3. EXCEPTION HANDLING : Comportement API REST (401 au lieu de 302)
                    .exceptionHandling(customizer -> customizer
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    )

                    // 4. AUTORISATIONS : Règles d'accès aux routes
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/public").permitAll()
                            .anyRequest().authenticated()
                    )

                    // 5. OAUTH2 / OIDC : Connexion et synchronisation
                    .oauth2Login(oauth2 -> oauth2
                            // Redirection vers le Front-End après une connexion réussie
                            .successHandler(new SimpleUrlAuthenticationSuccessHandler("http://localhost:4200"))
                            .userInfoEndpoint(userInfo -> userInfo
                                    .oidcUserService(customOidcUserService)
                            )
                    )

                    // 6. LOGOUT : Déconnexion locale et RP-Initiated (JumpCloud)
                    .logout(logout -> logout
                            .logoutSuccessHandler(oidcLogoutSuccessHandler())
                            .invalidateHttpSession(true)
                            .clearAuthentication(true)
                            .deleteCookies("JSESSIONID") // On nettoie la session Tomcat
                    );

            return http.build();
        } catch (Exception e) {
            // Respect de SonarQube et du Clean Code
            throw new SecurityConfigurationException("Impossible d'initialiser la chaîne de sécurité Spring", e);
        }
    }

    // --- Méthodes utilitaires ---

    /**
     * Gère la déconnexion globale auprès du fournisseur d'identité (JumpCloud).
     */
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // Redirection après la déconnexion JumpCloud (peut être l'URL du Front-End)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

    /**
     * Gère les autorisations CORS pour les requêtes inter-domaines.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Indispensable pour l'envoi des cookies/jetons

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}