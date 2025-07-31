package com.jb.afrostyle.security;

import com.jb.afrostyle.config.CorsProperties;
import com.jb.afrostyle.user.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité centralisée pour le monolithe AfroStyle
 * Combine la sécurité de tous les modules (User, Salon, Payment, Booking)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CorsProperties corsProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false))
            .securityContext(securityContext -> securityContext
                .requireExplicitSave(false))
            
            .authorizeHttpRequests(authz -> authz
                // ENDPOINTS PUBLICS SANS AUTHENTIFICATION
                .requestMatchers("/actuator/**", "/error/**").permitAll()
                
                // AUTHENTIFICATION - PUBLIQUE
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register", "/api/auth/logout").permitAll()
                .requestMatchers("/api/auth/validate-reset-token", "/api/auth/reset-password").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
                // WEBSOCKET - PUBLIQUE 
                .requestMatchers("/ws/**", "/websocket/**").permitAll()
                
                // SALON ET SERVICES - LECTURE PUBLIQUE (pour utilisateurs non connectés)
                .requestMatchers(HttpMethod.GET, "/api/salons/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/settings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/service-offerings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/maps/**").permitAll()
                
                // PAIEMENTS - ENDPOINTS PUBLICS STRIPE
                .requestMatchers(HttpMethod.GET, "/api/payments/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/payments/test/publishable-key").permitAll()
                .requestMatchers("/api/payments/checkout/webhook").permitAll()
                
                // TEMPORAIRE : ASSOUPLI POUR DEBUG - SERA DURCI PLUS TARD
                .requestMatchers(HttpMethod.GET, "/api/availability/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bookings/slots/**").permitAll()
                
                // AUTHENTIFIÉS - USER ROLE (utilisateurs normaux)
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers("/api/users/**").hasAnyRole("CUSTOMER", "SALON_OWNER", "ADMIN")
                .requestMatchers("/api/bookings/**").hasAnyRole("CUSTOMER", "SALON_OWNER", "ADMIN")
                .requestMatchers("/api/payments/**").hasAnyRole("CUSTOMER", "SALON_OWNER", "ADMIN")
                .requestMatchers("/api/stripe/**").hasAnyRole("CUSTOMER", "SALON_OWNER", "ADMIN")
                
                // GESTION - SALON OWNERS ET ADMINS
                .requestMatchers(HttpMethod.POST, "/api/settings/**").hasAnyRole("SALON_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/settings/**").hasAnyRole("SALON_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/service-offerings/**").hasAnyRole("SALON_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/service-offerings/**").hasAnyRole("SALON_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/service-offerings/**").hasAnyRole("SALON_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/availability/**").hasAnyRole("SALON_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/availability/**").hasAnyRole("SALON_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/availability/**").hasAnyRole("SALON_OWNER", "ADMIN")

                // ADMINISTRATION - ADMINS SEULEMENT
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // PAR DÉFAUT - ACCÈS LIBRE (TEMPORAIRE POUR DEBUG)
                .anyRequest().permitAll()
            )
            
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origines autorisées - Externalisées dans CorsProperties
        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        
        // Méthodes HTTP autorisées - Externalisées dans CorsProperties
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        
        // Headers autorisés - Externalisées dans CorsProperties
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        
        // Headers exposés au client - Configuration statique pour l'API
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
            "Authorization", "Content-Disposition"
        ));
        
        // Autorise les cookies et credentials - Externalisé dans CorsProperties
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        
        // Durée de cache pour les requêtes CORS preflight - Externalisée dans CorsProperties
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}