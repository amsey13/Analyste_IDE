package com.example.backend.core.auth.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
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
                    // Allow requests from the Front-End
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                    // Secure data modification requests (POST, PUT, DELETE)
                    .csrf(csrf -> csrf
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    )



                    // REST API behavior (401 instead of 302)
                    .exceptionHandling(customizer -> customizer
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    )

                    // Route access rules
                    .authorizeHttpRequests(auth -> auth




                            .requestMatchers("/api/public", "/login/**", "/error").permitAll()
                            .requestMatchers("/api/kpi/**").permitAll()
                            .anyRequest().authenticated()
                    )

                    // 5. OAUTH2 / OIDC: Authentication and synchronization
                    .oauth2Login(oauth2 -> oauth2

                            .authorizationEndpoint(authorization -> authorization
                                    .authorizationRequestResolver(authorizationRequestResolver())
                            )

                            .successHandler(new SimpleUrlAuthenticationSuccessHandler("http://localhost:5173"))


                            .failureHandler(new SimpleUrlAuthenticationFailureHandler("http://localhost:5173/?error=authentification_echouee"))

                            .userInfoEndpoint(userInfo -> userInfo
                                    .oidcUserService(customOidcUserService)
                            )
                    )

                    // Local logout and RP-Initiated logout (JumpCloud)
                    .logout(logout -> logout
                            .logoutRequestMatcher(request -> "GET".equals(request.getMethod()) && request.getRequestURI().endsWith("/logout"))
                            .logoutSuccessUrl("http://localhost:5173")
                            .invalidateHttpSession(true)
                            .clearAuthentication(true)
                            .deleteCookies("JSESSIONID")
                    );

            return http.build();
        } catch (Exception e) {
            throw new SecurityConfigurationException("Impossible d'initialiser la chaîne de sécurité Spring", e);
        }
    }


    /**
     * Handles CORS authorization for cross-origin requests.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params -> params.put("prompt", "login"))
        );

        return resolver;
    }
}