package com.example.backend.core.auth.config;

//import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.example.backend.core.auth.exception.SecurityConfigurationException;
import com.example.backend.core.auth.service.CustomOidcUserService;

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
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    )



                    // 3. EXCEPTION HANDLING : Comportement API REST (401 au lieu de 302)
                    .exceptionHandling(customizer -> customizer
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    )

                    // 4. AUTORISATIONS : Règles d'accès aux routes
                    .authorizeHttpRequests(auth -> auth



                            // On autorise l'API publique, la page de login par défaut et la gestion des erreurs
                            .requestMatchers("/api/public", "/login/**", "/error").permitAll()
                            .requestMatchers("/api/kpi/**").permitAll()
                            .anyRequest().authenticated()
                    )

                    // 5. OAUTH2 / OIDC : Connexion et synchronisation
                    .oauth2Login(oauth2 -> oauth2
                            //On branche le forçage du login (prompt=login)
                            .authorizationEndpoint(authorization -> authorization
                                    .authorizationRequestResolver(authorizationRequestResolver())
                            )
                            // Redirection vers le Front-End après une connexion réussie
                            .successHandler(new SimpleUrlAuthenticationSuccessHandler("http://localhost:5173"))

                            // Redirection propre vers le Front-End en cas d'échec (Fini la page 404 blanche !)
                            .failureHandler(new SimpleUrlAuthenticationFailureHandler("http://localhost:5173/?error=authentification_echouee"))
                            // Récupération et synchronisation du profil utilisateur
                            .userInfoEndpoint(userInfo -> userInfo
                                    .oidcUserService(customOidcUserService)
                            )
                    )

                    // 6. LOGOUT : Déconnexion locale et RP-Initiated (JumpCloud)
                    .logout(logout -> logout
                            .logoutRequestMatcher(request -> "GET".equals(request.getMethod()) && request.getRequestURI().endsWith("/logout"))
                            //.logoutSuccessHandler(oidcLogoutSuccessHandler())
                            .logoutSuccessUrl("http://localhost:5173")
                            .invalidateHttpSession(true)
                            .clearAuthentication(true)
                            .deleteCookies("JSESSIONID") // On nettoie la session Tomcat
                    );

            return http.build();
        } catch (Exception e) {
            throw new SecurityConfigurationException("Impossible d'initialiser la chaîne de sécurité Spring", e);
        }
    }

    // --- Méthodes utilitaires ---

    /**
     * Gère la déconnexion globale auprès du fournisseur d'identité (JumpCloud).
     */
    // Fix me : Problème pour tuer la session JumpCloud
    /* private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // Redirection après la déconnexion JumpCloud (peut être l'URL du Front-End)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:5173/");
        return oidcLogoutSuccessHandler;
    }*/

    /**
     * Gère les autorisations CORS pour les requêtes inter-domaines.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Indispensable pour l'envoi des cookies/jetons

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository, "/oauth2/authorization");

        // NOUVEAU : On force l'ajout du paramètre prompt=login à la requête vers JumpCloud
        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params -> params.put("prompt", "login"))
        );

        return resolver;
    }
}