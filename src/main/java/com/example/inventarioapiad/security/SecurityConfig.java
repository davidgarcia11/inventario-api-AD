package com.example.inventarioapiad.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

// Configuración global de Spring Security.
// Define qué rutas son públicas, cuáles requieren JWT, y dónde se mete
// nuestro filtro personalizado dentro de la cadena de filtros.
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // BCrypt para hashear las contraseñas. Lo usa UsuarioService.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Es una API REST sin sesiones ni formularios web, así que
                // desactivamos CSRF y la sesión por defecto.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos: login, register y la documentación
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                         "/v3/api-docs/**").permitAll()
                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Todo lo demás necesita JWT válido
                        .anyRequest().authenticated()
                )
                // Por defecto, Spring devuelve 403 cuando no estás autenticado.
                // Cambiamos ese comportamiento para devolver 401, que es lo
                // que esperan los clientes de una API JWT.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))
                // Metemos nuestro filtro antes del filtro estándar de
                // usuario/contraseña para que rellene el SecurityContext
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cuando llega una petición sin token (o con token inválido) a un endpoint
    // protegido, devolvemos 401 con un JSON con el mismo formato que el resto
    // de errores de la API.
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"codigo\":401,\"mensaje\":\"No autenticado: se requiere token JWT válido\"}");
        };
    }
}
