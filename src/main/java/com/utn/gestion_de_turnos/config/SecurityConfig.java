package com.utn.gestion_de_turnos.config;

import com.utn.gestion_de_turnos.security.CustomUserDetailsService;
import com.utn.gestion_de_turnos.security.JwtCookieAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
public class SecurityConfig {

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
    }

    @Autowired
    private JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/api/auth/**",
                                "/error",
                                "/api/auth/register",
                                "/",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/salas/**").permitAll()
                        .requestMatchers("/admin/empleados").hasAuthority("ADMIN")
                        .requestMatchers("/admin/salas/**").hasAuthority("ADMIN")
                        .requestMatchers("/empleado/**").hasAuthority("EMPLEADO")
                        .requestMatchers("/cliente/reservas").hasAuthority("CLIENTE")
                        .requestMatchers("/api/salas/cliente/activas").hasAuthority("CLIENTE")
                        .requestMatchers("/api/reserva/**").hasAnyAuthority("CLIENTE", "EMPLEADO")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}