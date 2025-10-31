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

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // HABILITA CORS usando el bean de abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // swagger / api-docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/error",
                                "/",
                                "/static/**", "/css/**", "/js/**"
                        ).permitAll()

                        // auth public
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/api/auth/**",
                                "/api/auth/register"
                        ).permitAll()

                        // tus endpoints públicos (mientras probás)
                        .requestMatchers(HttpMethod.GET, "/api/salas/**").permitAll()
                        .requestMatchers("/admin/empleados").permitAll()
                        .requestMatchers("/admin/salas/**").permitAll()
                        .requestMatchers("/empleado/**").permitAll()
                        .requestMatchers("/cliente/reservas").permitAll()
                        .requestMatchers("/api/salas/cliente/activas").permitAll()
                        .requestMatchers("/api/reserva/**").permitAll()

                        // MUY IMPORTANTE: permitir preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // TODO: cuando cierres, cambiá esto por .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                // El filtro JWT debe dejar pasar si no hay token
                .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Si usás cookies JWT: NO usar "*", especificar orígenes
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",          // 8080, 4200, 5173, etc.
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true); // necesario si usás cookie JWT

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

/*
origina>>>>>>>>>>>>>>>>>
.requestMatchers(HttpMethod.GET, "/api/salas/**").permitAll()
.requestMatchers("/admin/empleados").hasAuthority("ADMIN")
.requestMatchers("/admin/salas/**").hasAuthority("ADMIN")
.requestMatchers("/empleado/**").hasAuthority("EMPLEADO")
.requestMatchers("/cliente/reservas").hasAuthority("CLIENTE")
.requestMatchers("/api/salas/cliente/activas").hasAuthority("CLIENTE")
.requestMatchers("/api/reserva/**").hasAnyAuthority("CLIENTE", "EMPLEADO")
// .anyRequest().authenticated()
*/
