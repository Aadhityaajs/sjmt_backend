package com.sjmt.SJMT.Security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Spring Security Configuration
 * @author SJMT Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${app.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;
    
    /**
     * Password encoder bean using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * Authentication provider configuration
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * CORS configuration for cross-origin requests
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Security filter chain configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // PUBLIC AUTH ENDPOINTS
                .requestMatchers(
                    "/",
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/auth/verify-email",
                    "/api/auth/refresh-token",
                    "/api/auth/resend-verification",
                    "/api/client-errors"          // frontend error reports — no auth needed
                ).permitAll()

                // ADMIN ONLY
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/attendance/**").hasRole("ADMIN")
                
                // SWAGGER (API docs) — authenticated users only; ACTUATOR — admin only
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").authenticated()
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // AUTHENTICATED USERS (privilege-based access handled by @PreAuthorize)
                .requestMatchers(
                    "/api/users/**",
                    "/api/auth/logout",
                    "/api/auth/change-password",
                    "/api/inventory/**",
                    "/api/categories/**",
                    "/api/subcategories/**",
                    "/api/units/**"
                ).authenticated()

                // EVERYTHING ELSE
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}